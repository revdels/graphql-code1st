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
package revdels.graphql.code1st.scalars;


//import graphql.scalar.GraphqlBigDecimalCoercing;
//import graphql.scalar.GraphqlBigIntegerCoercing;
//import graphql.scalar.GraphqlByteCoercing;
//import graphql.scalar.GraphqlCharCoercing;
//import graphql.scalar.GraphqlLongCoercing;
//import graphql.scalar.GraphqlShortCoercing;
import graphql.schema.GraphQLScalarType;

public class NonStandardScalars {
	private NonStandardScalars() {}
/*
    public static final GraphQLScalarType GraphQLLong = GraphQLScalarType.newScalar()
            .name("Long").description("Long type").coercing(new GraphqlLongCoercing()).build();

    public static final GraphQLScalarType GraphQLShort = GraphQLScalarType.newScalar()
            .name("Short").description("Built-in Short as Int").coercing(new GraphqlShortCoercing()).build();

    public static final GraphQLScalarType GraphQLByte = GraphQLScalarType.newScalar()
            .name("Byte").description("Built-in Byte as Int").coercing(new GraphqlByteCoercing()).build();

    public static final GraphQLScalarType GraphQLBigInteger = GraphQLScalarType.newScalar()
            .name("BigInteger").description("Built-in java.math.BigInteger").coercing(new GraphqlBigIntegerCoercing()).build();

    public static final GraphQLScalarType GraphQLBigDecimal = GraphQLScalarType.newScalar()
            .name("BigDecimal").description("Built-in java.math.BigDecimal").coercing(new GraphqlBigDecimalCoercing()).build();

    public static final GraphQLScalarType GraphQLChar = GraphQLScalarType.newScalar()
            .name("Char").description("Built-in Char as Character").coercing(new GraphqlCharCoercing()).build();
 */   
    public static final GraphQLScalarType GraphQLInstant = GraphQLScalarType.newScalar()
            .name("Instant").description("Built-in java.type.Instant").coercing(new GraphqlInstantCoercing()).build();
    

}
