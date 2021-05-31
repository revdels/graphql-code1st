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
import java.util.HashMap;
import java.util.Map;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLObjectType.Builder;
import revdels.graphql.code1st.annotations.GraphQLMutation;
import revdels.graphql.code1st.annotations.GraphQLQuery;
import revdels.graphql.code1st.annotations.GraphQLSubscription;
import revdels.graphql.code1st.api.GraphQLSchemaGenerator;
import revdels.graphql.code1st.fetchers.GenericDataFetcher;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLSchema;

public class GraphQLSchemaGeneratorImpl implements GraphQLSchemaGenerator {
	
	private GraphQLCodeRegistry.Builder codeRegistryBuilder;
	private GraphQLObjectType.Builder queryBuilder;
	private GraphQLObjectType.Builder mutationBuilder;
	private GraphQLObjectType.Builder subscriptionBuilder;
	private GraphQLObjectType query;
	private GraphQLObjectType mutation;
	private GraphQLObjectType subscription;
	private Map<String, GenericDataFetcher> queryDataFetchers = new HashMap<>();
	private Map<String, GenericDataFetcher> mutationDataFetchers = new HashMap<>();
	private Map<String, GenericDataFetcher> subscriptionDataFetchers = new HashMap<>();
	private GeneratedTypes generatedTypes;
	
	private GraphQLTypeGenerator typeGenerator;

	public GraphQLSchemaGeneratorImpl() {
	    this.codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();
	    this.generatedTypes = new GeneratedTypes();
	    this.typeGenerator = GraphQLTypeGeneratorImpl.newTypeGenerator(codeRegistryBuilder, generatedTypes);
	}
	
	@Override
	public GraphQLSchema generate() {
		if (queryBuilder != null) {
			query = queryBuilder.build();
			query.getFieldDefinitions().forEach(this::registerQueryDataFetcher);
		}
		if (mutationBuilder != null) {
			mutation = mutationBuilder.build();
			mutation.getFieldDefinitions().forEach(this::registerMutationDataFetcher);
		}
		if (subscriptionBuilder != null) {
			subscription = subscriptionBuilder.build();
			subscription.getFieldDefinitions().forEach(this::registerSubscriptionDataFetcher);			
		}
		return GraphQLSchema.newSchema()
			.query(query)
    		.mutation(mutation)
    		.subscription(subscription)
			.codeRegistry(codeRegistryBuilder.build())
    		.build();
	}
	
	@Override
	public void addController(String name, Object controller) {
		Class<?> type = controller.getClass();
		for (Method method : type.getMethods()) {
			GraphQLQuery queryAnnotation = method.getAnnotation(GraphQLQuery.class);
			if (queryAnnotation != null) {
				generateQuery(queryAnnotation,controller,  method);
			}
			GraphQLMutation mutationAnnotation = method.getAnnotation(GraphQLMutation.class);
			if (mutationAnnotation != null) {
				generateMutation(mutationAnnotation, controller, method);
			}
			GraphQLSubscription subscriptionAnnotation = method.getAnnotation(GraphQLSubscription.class);
			if (subscriptionAnnotation != null) {
				generateSubscription(subscriptionAnnotation, controller, method);
			}
		}
	}

	private void registerQueryDataFetcher(GraphQLFieldDefinition fieldDefinition) {
		String name = fieldDefinition.getName();
		codeRegistryBuilder.dataFetcher(query, fieldDefinition, queryDataFetchers.get(name));
	}
	
	private void registerMutationDataFetcher(GraphQLFieldDefinition fieldDefinition) {
		String name = fieldDefinition.getName();
		codeRegistryBuilder.dataFetcher(mutation, fieldDefinition, mutationDataFetchers.get(name));
	}
	
	private void registerSubscriptionDataFetcher(GraphQLFieldDefinition fieldDefinition) {
		String name = fieldDefinition.getName();
		codeRegistryBuilder.dataFetcher(subscription, fieldDefinition, subscriptionDataFetchers.get(name));
	}
	
	private void generateQuery(GraphQLQuery queryAnnotation, Object controller, Method method) {
		String name = queryAnnotation.name();
		if (name.isEmpty()) {
			name = method.getName();
		}
		GraphQLOutputType type = typeGenerator.generateOutputType(method.getGenericReturnType());
		String description = queryAnnotation.description();
		if (description.isEmpty()) {
			description = String.format("%s: enter %s value", name, type.toString());
		}
		GraphQLFieldDefinition queryField = GraphQLFieldDefinition.newFieldDefinition()
    		.name(name)
    		.type(type)
    		.description(description)
    		.arguments(typeGenerator.genInputArguments(method))
    		.build();
    	if (queryBuilder == null) {
    		queryBuilder = createQueryBuilder();
    	}
    	queryBuilder.field(queryField);
    	queryDataFetchers.put(name,  new GenericDataFetcher(generatedTypes, controller, method));
	}
	
	private void generateMutation(GraphQLMutation mutationAnnotation, Object controller, Method method) {
		String name = mutationAnnotation.name();
		if (name.isEmpty()) {
			name = method.getName();
		}
		GraphQLOutputType type = typeGenerator.generateOutputType(method.getGenericReturnType());
		String description = mutationAnnotation.description();
		if (description.isEmpty()) {
			description = String.format("%s: enter %s value", name, type.toString());
		}
    	GraphQLFieldDefinition mutationField = GraphQLFieldDefinition.newFieldDefinition()
    		.name(name)
    		.type(type)
       		.description(description)
    		.arguments(typeGenerator.genInputArguments(method))
       	    .build();
    	if (mutationBuilder == null) {
    		mutationBuilder = createMutationBuilder();
    	}
    	mutationBuilder.field(mutationField);
    	mutationDataFetchers.put(name,  new GenericDataFetcher(generatedTypes, controller, method));
	}
	
	private void generateSubscription(GraphQLSubscription mutationAnnotation, Object controller, Method method) {
		String name = mutationAnnotation.name();
		if (name.isEmpty()) {
			name = method.getName();
		}
		GraphQLOutputType type = typeGenerator.generateSubscriptionType(method.getGenericReturnType());
		String description = mutationAnnotation.description();
		if (description.isEmpty()) {
			description = String.format("%s: enter %s value", name, type.toString());
		}
    	GraphQLFieldDefinition subscriptionField = GraphQLFieldDefinition.newFieldDefinition()
    		.name(name)
    		.type(type)
       		.description(description)
    		.arguments(typeGenerator.genInputArguments(method))
       	    .build();
    	if (subscriptionBuilder == null) {
    		subscriptionBuilder = createSubscriptionBuilder();
    	}
    	subscriptionBuilder.field(subscriptionField);
    	subscriptionDataFetchers.put(name,  new GenericDataFetcher(generatedTypes, controller, method));
	}



	private  GraphQLObjectType.Builder createQueryBuilder() {
		return GraphQLObjectType.newObject()
 	    		.name("Query");		
	}

	private Builder createMutationBuilder() {
		return GraphQLObjectType.newObject()
 	    		.name("Mutation");
	}

	private Builder createSubscriptionBuilder() {
		return GraphQLObjectType.newObject()
 	    		.name("Subscription");
	}

}
