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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;

public interface GraphQLTypeGenerator {

	GraphQLOutputType generateOutputType(Type type);

	GraphQLInputType generateInputType(Type type);

	GraphQLOutputType generateSubscriptionType(Type type);
	
	boolean isDefinedType(String typeName, Class<?> type);

	List<GraphQLArgument> genInputArguments(Method method);

	GeneratedTypes getGeneratedTypes();

}