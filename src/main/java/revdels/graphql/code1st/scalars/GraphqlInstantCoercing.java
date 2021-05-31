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

import java.time.Instant;
import java.util.Optional;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;

public class GraphqlInstantCoercing implements Coercing<Instant, Instant> {

	private Optional<Instant> convert(Object input) {
		if (input instanceof String) {
			return Optional.of(Instant.parse((String)input));
		}
		if (input instanceof Instant) {
			return Optional.of((Instant) input);
		}
		return Optional.empty();
	}

	
	@Override
	public Instant serialize(Object input) {
		return convert(input)
				.orElseThrow( () -> new CoercingSerializeException(
                "Expected type 'Instant' but was '" + input.getClass().getSimpleName() + "'."));
	}

	@Override
	public Instant parseValue(Object input) {
		return convert(input)
				.orElseThrow( () -> new CoercingParseValueException(
                "Expected type 'Instant' but was '" + input.getClass().getSimpleName() + "'."));
	}

	@Override
	public Instant parseLiteral(Object input) {
		if (input instanceof StringValue) {
			String stringValue = ((StringValue) input).getValue();
			return convert(stringValue)
					.orElseThrow(() -> new CoercingParseLiteralException(
		            "Unable to turn AST input into a 'Instant' : '" + stringValue + "'"));		                       					
		}
        throw new CoercingParseLiteralException(
                "Expected AST type 'StringValue' but was '" + input.getClass().getSimpleName() + "'."
        );
	 }

}
