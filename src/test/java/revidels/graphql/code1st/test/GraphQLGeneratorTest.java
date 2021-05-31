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
package revidels.graphql.code1st.test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import revdels.graphql.code1st.annotations.GraphQLQuery;
import revdels.graphql.code1st.api.GraphQLSchemaGenerator;

class GraphQLGeneratorTest {
	public static class HelloController {
		
		@GraphQLQuery
		public String hello(String greet) {
			return String.format("Hello %s!", greet);			
		}
	}
	
	@Test
	void helloWorldTest() {
		GraphQLSchemaGenerator gen = GraphQLSchemaGenerator.newGraphQLSchemaGenerator();
		gen.addController("Hello", new HelloController());
		GraphQLSchema schema = gen.generate();
		GraphQLObjectType queries = schema.getQueryType();
		assertEquals(1, queries.getChildren().size());
	
	
	}
	
	
	
	
}
