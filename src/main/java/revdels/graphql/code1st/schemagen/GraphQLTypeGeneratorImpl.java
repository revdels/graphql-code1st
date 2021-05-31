/**
 * Copyright 2021 revdels (https://github.com/revdels)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package revdels.graphql.code1st.schemagen;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLEnumType.Builder;
import revdels.graphql.code1st.annotations.GraphQLEnum;
import revdels.graphql.code1st.annotations.GraphQLEnumValue;
import revdels.graphql.code1st.annotations.GraphQLParam;
import revdels.graphql.code1st.exceptions.UnsuportedTypeException;
import revdels.graphql.code1st.scalars.NonStandardScalars;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;

public class GraphQLTypeGeneratorImpl implements GraphQLTypeGenerator {

	private GeneratedTypes generatedTypes;
	private GraphQLOutputObjectGenerator outputObjectGenerator;
	private GraphQLInputObjectGenerator inputObjectGenerator;
	
	public GraphQLTypeGeneratorImpl(GraphQLCodeRegistry.Builder codeRegistryBuilder, GeneratedTypes generatedTypes) {
		this.generatedTypes = generatedTypes;
		outputObjectGenerator = new GraphQLOutputObjectGenerator(this, codeRegistryBuilder);
		inputObjectGenerator = new GraphQLInputObjectGenerator(this);
	}
	
	private interface OutputTypeGenerator {
		GraphQLOutputType generate(Type type);
	}
	
	private interface InputTypeGenerator {
		GraphQLInputType generate(Type type);
	}
	
	private final OutputTypeGenerator[] outputTypeGenerators =  
		{ this::genOutputList, this::genOutputEnum, this::genScalar, this::genOutputObject};
	
	private final InputTypeGenerator[] inputTypeGenerators =  
		{ this::genInputList, this::genInputEnum, this::genScalar, this::genInputObject};
	
	@Override
	public GraphQLOutputType generateOutputType(Type type) {
		
		return Arrays.stream(outputTypeGenerators)
				.map(t -> t.generate(type))
				.filter(Objects::nonNull)
				.findFirst()
				.orElseThrow(() -> new UnsuportedTypeException(type.getTypeName()));
	}
	
	@Override
	public GraphQLInputType generateInputType(Type type) {
		
		return Arrays.stream(inputTypeGenerators)
				.map(t -> t.generate(type))
				.filter(Objects::nonNull)
				.findFirst()
				.orElseThrow(() -> new UnsuportedTypeException(type.getTypeName()));
	}
	
	@Override
	public GraphQLOutputType generateSubscriptionType(Type type) {
		if (! (type instanceof ParameterizedType)) {
			throw new UnsuportedTypeException(type.getTypeName());
		}
					
		ParameterizedType ptype = ((ParameterizedType) type);
		if (! ptype.getRawType().getTypeName().equals("reactor.core.publisher.Flux")) {
			throw new UnsuportedTypeException(type.getTypeName());
		}
	
		Type [] params = ptype.getActualTypeArguments();	
		if (params == null || params.length == 0) {
			throw new UnsuportedTypeException("Flux must have parameter");				
		}
		Type publishType = params[0];			
		
		
		return Arrays.stream(outputTypeGenerators)
				.map(t -> t.generate(publishType))
				.filter(Objects::nonNull)
				.findFirst()
				.orElseThrow(() -> new UnsuportedTypeException(type.getTypeName()));
	}
	
	
	
	private enum ListType { INPUT_LIST, OUTPUT_LIST }
	
	private GraphQLList genOutputList(Type type) {
		return genList(type, ListType.OUTPUT_LIST);
	}
	
	private GraphQLList genInputList(Type type) {
		return genList(type, ListType.INPUT_LIST);
	}
	
	private GraphQLList genList(Type type, ListType listType) {
		if (type instanceof ParameterizedType) {
			ParameterizedType ptype = (ParameterizedType) type;
			if (!ptype.getRawType().equals(List.class)) {
				throw new UnsuportedTypeException(type.getTypeName());
			}
			Type [] params = ptype.getActualTypeArguments();	
			if (params == null || params.length == 0) {
				throw new UnsuportedTypeException("List must have parameter");				
			}
			if (listType == ListType.OUTPUT_LIST) {
				return GraphQLList.list(generateOutputType(params[0]));
			}
			else {
				return GraphQLList.list(generateInputType(params[0]));				
			}
		}
		return null;
	}
	
	
	private GraphQLInputType genInputEnum(Type type) {
		return (GraphQLInputType) genEnum(type);
	}
	
	private GraphQLOutputType genOutputEnum(Type type) {
		return (GraphQLOutputType) genEnum(type);
	}
	
	private GraphQLType genEnum(Type type) {
		if (type instanceof Class<?> && ((Class<?>)type).isEnum()) {
			Class<?> enumType = (Class<?>) type;
			GraphQLEnum enumAnnotation = enumType.getAnnotation(GraphQLEnum.class);
			String name = enumAnnotation != null ? enumAnnotation.name() : "";
			if (name.isEmpty()) {
				name = enumType.getSimpleName();
			}
			if (generatedTypes.isDefinedType(name, enumType)) {
				return new GraphQLTypeReference(name);
			}
			GraphQLEnumType.Builder newEnum = GraphQLEnumType.newEnum();
			newEnum.name(name);
			String description = enumAnnotation != null ? enumAnnotation.description() : "";
			if (description.isEmpty()) {
				description = name;
			}
			newEnum.description(description);
			Arrays.stream(enumType.getEnumConstants()).forEach(val -> enumValue(newEnum, val));
			return newEnum.build();
		}
		return null;
	}
	
	private void enumValue(Builder newEnum, Object val) {
		Class<?> type = val.getClass();
		String name = ((Enum<?>)val).name();
		Field field = null;
		try {
			field = type.getField(name);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new IllegalStateException("Cannot occur");
		}
		GraphQLEnumValue annotation = field.getAnnotation(GraphQLEnumValue.class);
		String label = annotation != null ? annotation.label() : "";
		if (label.isEmpty()) {
			label = name;
		}
		newEnum.value(label, val);
	}

	private GraphQLScalarType genScalar(Type type) {
		switch (type.getTypeName()) {		
			case "int":
			case "java.lang.Integer":
				return Scalars.GraphQLInt;
		
			case "long":
			case "java.lang.Long":
				return Scalars.GraphQLLong;
		
			case "java.lang.String":
				return Scalars.GraphQLString;

			case "boolean":
			case "java.lang.Boolean":
				return Scalars.GraphQLBoolean;

			case "double":
			case "java.lang.Double":
			case "float":
			case "java.lang.Float":
				return Scalars.GraphQLFloat;
				
				
			case "java.time.Instant":
			case "java.util.Date":
				return NonStandardScalars.GraphQLInstant;
				

			default:
				return null;
		}
	}
	
	private GraphQLOutputType genOutputObject(Type type) {
		if (type instanceof Class<?>) {
			return outputObjectGenerator.generate((Class<?>) type);
		}
		return null;
	}
	
	
	@Override
	public List<GraphQLArgument> genInputArguments(Method method) {
		return Arrays.stream(method.getParameters())
				.map(this::genInputArgument)
				.collect(Collectors.toList()
				);
	}
	
	private GraphQLArgument genInputArgument(Parameter parameter) {
		String argName = makeArgName(parameter);
		String argDescription = makeArgDescription(parameter, argName);
		GraphQLInputType argType = generateInputType(parameter.getParameterizedType());
		return GraphQLArgument.newArgument()
			.name(argName)
			.description(argDescription)
			.type(argType)
			.build();
	}


	private String makeArgName(Parameter parameter) {
		GraphQLParam paramAnnotation = parameter.getAnnotation(GraphQLParam.class);
		if (paramAnnotation != null) {
			String paramName = paramAnnotation.name();
			if (! paramName.isEmpty()) {
				return paramName;
			}
		}
		return parameter.getName();
	}

	private String makeArgDescription(Parameter parameter, String defaultDescription) {
		GraphQLParam paramAnnotation = parameter.getAnnotation(GraphQLParam.class);
		if (paramAnnotation != null) {
			String paramDescription = paramAnnotation.description();
			if (! paramDescription.isEmpty()) {
				return paramDescription;
			}
		}
		return defaultDescription;
	}


	private GraphQLInputType genInputObject(Type type) { 
		if (type instanceof Class<?>) {
			return inputObjectGenerator.generate((Class<?>) type);
		}
		return null;
	}


	public static GraphQLTypeGenerator newTypeGenerator(GraphQLCodeRegistry.Builder codeRegistryBuilder, GeneratedTypes generatedTypes) {
		return new GraphQLTypeGeneratorImpl(codeRegistryBuilder, generatedTypes);
	}

	@Override
	public boolean isDefinedType(String name, Class<?> type) {
		return generatedTypes.isDefinedType(name, type);
	}

	@Override
	public GeneratedTypes getGeneratedTypes() {
		return generatedTypes;
	}

}