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

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLSchemaElement;
import graphql.schema.GraphQLTypeVisitorStub;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import revdels.graphql.code1st.schemagen.GeneratedTypes;

public class GraphQLArgumentFetcher extends GraphQLTypeVisitorStub {

	private DataFetchingEnvironment environment;
	private GraphQLInputFetcher inputFetcher;
	private GeneratedTypes generatedTypes;

	public GraphQLArgumentFetcher(GeneratedTypes generatedTypes, DataFetchingEnvironment environment) {
		this.generatedTypes = generatedTypes;
		this.environment = environment;
	}

	@Override
	public TraversalControl visitGraphQLArgument(GraphQLArgument argDef, TraverserContext<GraphQLSchemaElement> context) {
		Object rawValue = environment.getArgument(argDef.getName());
		GraphQLInputType type = argDef.getType();
		inputFetcher = new GraphQLInputFetcher(generatedTypes, rawValue);
		return type.accept(context, inputFetcher);
	}

	public Object getValue() {
		return inputFetcher == null ? null : inputFetcher.getValue();
	}

}
