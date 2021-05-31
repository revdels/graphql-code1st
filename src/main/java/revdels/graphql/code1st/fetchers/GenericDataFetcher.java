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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import revdels.graphql.code1st.schemagen.GeneratedTypes;

public class GenericDataFetcher implements DataFetcher<Object> {

	private Object object;
	private Method method;
	private GeneratedTypes generatedTypes;

	public GenericDataFetcher(GeneratedTypes generatedTypes, Object object, Method method) {
		this.generatedTypes = generatedTypes;
		this.object = object;
		this.method = method;
	}

	public GenericDataFetcher(GeneratedTypes generatedTypes, Method method) {
		this(generatedTypes, null, method);
	}

	@Override
	public Object get(DataFetchingEnvironment environment) throws Exception {
		
		Object source = object != null ? object : environment.getSource();
		List<GraphQLArgument> argDefs = environment.getFieldDefinition().getArguments();
		GraphQLArgumentFetcher argFetcher = new GraphQLArgumentFetcher(generatedTypes, environment);
		Object[] args = new Object[argDefs.size()];
		for (int i = 0; i < args.length; i++) {
			GraphQLArgument argDef = argDefs.get(i);
			argDef.accept(null, argFetcher);
			args[i] = argFetcher.getValue();
		}
		try {
			return method.invoke(source,  args);
		}
		catch (InvocationTargetException e) {
			 Throwable cause = e.getTargetException();
			if (cause instanceof Exception) {
				throw (Exception) cause;
			}
			throw e;
		}
	
	}
}
