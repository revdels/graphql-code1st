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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import revdels.graphql.code1st.annotations.GraphQLField;
import revdels.graphql.code1st.annotations.GraphQLSkip;
import revdels.graphql.code1st.annotations.GraphQLType;

public class GraphQLAbstractObjectGenerator {

	private String objectTypeNamePostfix;
	
	public GraphQLAbstractObjectGenerator(String objectTypeNamePostfix) {
		this.objectTypeNamePostfix = objectTypeNamePostfix;
	}

	protected static final Pattern getterPattern = Pattern.compile("^((get)|(is))(?<name>[A-Z].*)$");
	protected static final Pattern setterPattern = Pattern.compile("^set(?<name>[A-Z].*)$");



	protected class ObjectInfo {
		private final Class<?> clazz;
		private final GraphQLType typeAnnotation;
		private String name;
		private String description;
		
		ObjectInfo(Class<?> clazz) {
			this.clazz = clazz;
			this.typeAnnotation = clazz.getAnnotation(GraphQLType.class);
			makeName();
			makeDescription();
		}
		
		private void makeName() {
			name = (typeAnnotation == null) ? null : typeAnnotation.name();
			if (name == null || name.isEmpty()) {
				name = clazz.getSimpleName() + objectTypeNamePostfix;
			}
		}
		
		private void makeDescription() {
			description = (typeAnnotation == null) ? null : typeAnnotation.description();
			if (description == null || description.isEmpty()) {
				description = name;
			}
		}
		
		List<FieldInfo> getOutputFields() {
			return Arrays.stream(clazz.getMethods())
			.filter(method -> ! method.getDeclaringClass().equals(Object.class))
			.filter(method -> method.getAnnotation(GraphQLSkip.class) == null)
			.map(FieldInfo::new)
			.filter(FieldInfo::isOutputField)
			.collect(Collectors.toList());
		}

		List<FieldInfo> getInputFields() {
			return Arrays.stream(clazz.getMethods())
			.filter(method -> ! method.getDeclaringClass().equals(Object.class))
			.filter(method -> method.getAnnotation(GraphQLSkip.class) == null)
			.map(FieldInfo::new)
			.filter(FieldInfo::isInputField)
			.collect(Collectors.toList());
		}

		public String getDescription() {
			return description;
		}

		public String getName() {
			return name;
		}

	}

	private enum FieldType { GETTER, SETTER, OTHER };
	

	protected static class FieldInfo {
		private Method method;
		GraphQLField fieldAnnotation;

		private String name;
		private FieldType fieldType;
		private String description;

		FieldInfo(Method method) {
			this.method = method;
			this.fieldAnnotation = method.getAnnotation(GraphQLField.class);
			makeName();
			makeDescription();
		}


		private void makeName() {
			fieldType = FieldType.OTHER;
			Matcher matcher = getterPattern.matcher(method.getName());
			if (matcher.matches()) {
				String fieldName = matcher.group("name");
				name = fieldName.substring(0,1).toLowerCase() + fieldName.substring(1);
				if ( method.getParameterCount() == 0 ) {
					fieldType = FieldType.GETTER;
				}
			}
			else {
				matcher = setterPattern.matcher(method.getName());
				if (matcher.matches()) {
					String fieldName = matcher.group("name");					
					name = fieldName.substring(0,1).toLowerCase() + fieldName.substring(1);
					if (method.getParameterCount() == 1) {
						fieldType = FieldType.SETTER;
					}
				}
				else {
					name = method.getName();
				}
			}
			if (fieldAnnotation != null) {
				String overrideName = fieldAnnotation.name();
				if (!overrideName.isEmpty()) {
					name = overrideName;
					fieldType = FieldType.OTHER;
				}
			}
		}
		
		boolean isOutputField() {
			return fieldType == FieldType.GETTER || fieldType == FieldType.OTHER;
		}
		
		boolean isInputField() {
			return fieldType == FieldType.SETTER;
		}
		
		Type getDataType() {
			if (fieldType == FieldType.SETTER) {
				return method.getParameters()[0].getParameterizedType();
			}
			else {
				return method.getGenericReturnType();
			}
		}
		
		private void makeDescription() {
			if (fieldAnnotation != null) {
				description = (fieldAnnotation.description());
				if (!description.isEmpty()) {
					return;
				}
			}
			description = name;		
		}

		public String getDescription() {
			return description;
		}


		public String getName() {
			return name;
		}

		public boolean useDefaultFetcher() {
			return fieldType == FieldType.GETTER;
		}

		public Method getMethod() {
			return method;
		}
	}


}
