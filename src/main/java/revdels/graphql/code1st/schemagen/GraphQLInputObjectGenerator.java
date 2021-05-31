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

import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLTypeReference;

public class GraphQLInputObjectGenerator extends GraphQLAbstractObjectGenerator {

	private GraphQLTypeGenerator typeGenerator;


	public GraphQLInputObjectGenerator(GraphQLTypeGenerator typeGenerator) {
		super("_IN");
		this.typeGenerator = typeGenerator;
	}
	
	public GraphQLInputType generate(Class<?> type) {
		ObjectInfo objectInfo = new ObjectInfo(type);
		if (typeGenerator.isDefinedType(objectInfo.getName(), type)) {
			return GraphQLTypeReference.typeRef(objectInfo.getName());
		}
		return generateObject(objectInfo);
		
	}
		
	private GraphQLInputType generateObject(ObjectInfo objectInfo) {
		GraphQLInputObjectType.Builder object = GraphQLInputObjectType.newInputObject();
		object.name(objectInfo.getName());
		object.description(objectInfo.getDescription());
		objectInfo.getInputFields().forEach(fieldInfo -> object.field(generateField(fieldInfo)));
		return object.build();
	}

	private GraphQLInputObjectField generateField(FieldInfo fieldInfo) {
		GraphQLInputObjectField.Builder field = GraphQLInputObjectField.newInputObjectField();
		field.name(fieldInfo.getName());
		field.type(typeGenerator.generateInputType(fieldInfo.getDataType()));
		field.description(fieldInfo.getDescription());
		return field.build();
	}
	
}
