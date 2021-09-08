/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbehave.core.model;

import java.util.Optional;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.TablePropertiesQueue;
import org.jbehave.core.steps.ParameterConverters;
import org.vividus.bdd.steps.VariableResolver;

public class VariableResolvingTableParsers extends TableParsers
{
    private final VariableResolver variableResolver;

    public VariableResolvingTableParsers(VariableResolver variableResolver, Keywords keywords,
            ParameterConverters parameterConverters, Optional<String> defaultNullPlaceholder)
    {
        super(keywords, parameterConverters, defaultNullPlaceholder);
        this.variableResolver = variableResolver;
    }

    @Override
    public TablePropertiesQueue parseProperties(String tableAsString)
    {
        return super.parseProperties((String) variableResolver.resolve(tableAsString));
    }
}