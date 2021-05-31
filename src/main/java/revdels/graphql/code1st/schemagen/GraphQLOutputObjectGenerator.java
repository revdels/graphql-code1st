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

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeReference;
import revdels.graphql.code1st.fetchers.GenericDataFetcher;

public class GraphQLOutputObjectGenerator extends GraphQLAbstractObjectGenerator {
	
	private GraphQLCodeRegistry.Builder codeRegistryBuilder;
	private GraphQLTypeGenerator typeGenerator;
	
	public GraphQLOutputObjectGenerator(GraphQLTypeGenerator typeGeneratorl, GraphQLCodeRegistry.Builder codeRegistryBuilder) {
		super("");
		this.typeGenerator = typeGeneratorl;
		this.codeRegistryBuilder = codeRegistryBuilder;
	}
	
	public GraphQLOutputType generate(Class<?> type) {
		ObjectInfo objectInfo = new ObjectInfo(type);
		if (typeGenerator.isDefinedType(objectInfo.getName(), type)) {
			return GraphQLTypeReference.typeRef(objectInfo.getName());
		}
		return generateObject(objectInfo);
	}

	private GraphQLOutputType generateObject(ObjectInfo objectInfo) {
		GraphQLObjectType.Builder object = GraphQLObjectType.newObject();
		object.name(objectInfo.getName());
		object.description(objectInfo.getDescription());
		objectInfo.getOutputFields().forEach(fieldInfo -> object.field(generateField(objectInfo, fieldInfo)));
		return object.build();
	}

	private GraphQLFieldDefinition generateField(ObjectInfo objectInfo, FieldInfo fieldInfo) {
		GraphQLFieldDefinition.Builder field = GraphQLFieldDefinition.newFieldDefinition();
		GraphQLFieldDefinition fieldDef = field.name(fieldInfo.getName())
			 .type(typeGenerator.generateOutputType(fieldInfo.getDataType()))
			 .description(fieldInfo.getDescription())
			 .arguments(typeGenerator.genInputArguments(fieldInfo.getMethod()))
			 .build();
		if (!fieldInfo.useDefaultFetcher()) {
			codeRegistryBuilder.dataFetcher(
				FieldCoordinates.coordinates( objectInfo.getName(), fieldInfo.getName()), 
				new GenericDataFetcher(typeGenerator.getGeneratedTypes(), fieldInfo.getMethod()));
		}
		return fieldDef;
	}

}
