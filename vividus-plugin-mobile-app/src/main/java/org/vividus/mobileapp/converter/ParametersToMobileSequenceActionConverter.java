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

package org.vividus.mobileapp.converter;

import javax.inject.Named;

import com.google.common.reflect.TypeToken;

import org.vividus.converter.ui.AbstractParametersToSequenceActionConverter;
import org.vividus.converter.ui.StringToLocatorConverter;
import org.vividus.mobileapp.action.MobileSequenceActionType;
import org.vividus.steps.ui.model.SequenceAction;

@Named
public class ParametersToMobileSequenceActionConverter
        extends AbstractParametersToSequenceActionConverter<MobileSequenceActionType>
{
    public ParametersToMobileSequenceActionConverter(StringToLocatorConverter stringToLocatorConverter)
    {
        super(stringToLocatorConverter, new TypeToken<SequenceAction<MobileSequenceActionType>>() { }.getType());
    }
}
