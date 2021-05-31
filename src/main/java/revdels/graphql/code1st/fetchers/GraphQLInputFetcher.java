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
package revdels.graphql.code1st.fetchers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchemaElement;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeVisitorStub;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import revdels.graphql.code1st.schemagen.GeneratedTypes;

public class GraphQLInputFetcher extends GraphQLTypeVisitorStub {

	private Object rawValue;
	private Object value;
	private GeneratedTypes generatedTypes;

	public GraphQLInputFetcher(GeneratedTypes generatedTypes, Object rawValue) {
		this.generatedTypes = generatedTypes;
		this.rawValue = rawValue;
	}
	
	@Override
	public TraversalControl visitGraphQLScalarType(GraphQLScalarType scalarType,
			TraverserContext<GraphQLSchemaElement> context) {
		value = scalarType.getCoercing().parseValue(rawValue);
		return TraversalControl.CONTINUE;
	}
	
	@Override
	public TraversalControl visitGraphQLList(GraphQLList list, TraverserContext<GraphQLSchemaElement> context) {
		List<?> rawList = (List<?>) rawValue;
		List<Object> result = new ArrayList<>();
		GraphQLType listType = list.getWrappedType();
		for (Object rawItem : rawList) {
			GraphQLInputFetcher itemFetcher = new GraphQLInputFetcher(generatedTypes, rawItem);
			listType.accept(context, itemFetcher);
			result.add(itemFetcher.getValue());
		}
		value = result;
		return TraversalControl.CONTINUE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public TraversalControl visitGraphQLInputObjectType(GraphQLInputObjectType objectType,TraverserContext<GraphQLSchemaElement> context) {
		Class<?> javaType = generatedTypes.getJavaType(objectType.getName());

		value = createObject(javaType);
		for (Entry<String, Object> field : ((Map<String, Object>) rawValue).entrySet()) {
			GraphQLInputType fieldType = objectType.getFieldDefinition(field.getKey()).getType();
			GraphQLInputFetcher fieldFetcher = new GraphQLInputFetcher(generatedTypes, field.getValue());
			fieldType.accept(context, fieldFetcher);
			setAttribute(field.getKey(), fieldFetcher.getValue());
		}
		return TraversalControl.CONTINUE;
	}

	private void setAttribute(String fieldName, Object fieldValue) {
		String setterName = "set" + 
			   fieldName.substring(0,1).toUpperCase() +
			   fieldName.substring(1);		
		Method setter = 
				Arrays.stream(value.getClass().getMethods())
					.filter(method -> method.getName().equals(setterName))
					.findFirst()
					.orElseThrow(() -> new IllegalStateException("No setter: " + setterName));
		try {
			setter.invoke(value, fieldValue);
		}
		catch (Exception e) {
			throw new IllegalStateException("Setter failed: " + setterName, e);
		}
	}

	private Object createObject(Class<?> javaType) {
		try {
			Constructor<?> constructor = javaType.getConstructor();
			return constructor.newInstance();
		}
		catch (Exception e) {
			throw new IllegalStateException("Construction failed: " + javaType.toString(), e);
		}
	}

	@Override
    public TraversalControl visitGraphQLEnumType(GraphQLEnumType enumType, TraverserContext<GraphQLSchemaElement> context) {
    	value = rawValue;
    	return TraversalControl.CONTINUE;    	
    }

	Object getValue() {
		return value;
	}

}
