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
package revdels.graphql.code1st.exceptions;

public class DuplicateTypeException extends RuntimeException {

	public DuplicateTypeException(String name, Class<?> type1, Class<?> type2) {
		super(String.format("Conflicting names for GraphQL type %s: %s and %s", name, type1.toString(), type2.toString()));
	}

	private static final long serialVersionUID = 1L;

}
