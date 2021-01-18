/*
 * Copyright (c) 2019 Bynder B.V. All rights reserved.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */
package com.bynder.sdk.query.decoder;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts parameter name from string to "property_name" to send to API.
 */
public class MetapropertyParameterDecoder implements ParameterDecoder<Map<String, String>> {

    @Override
    public Map<String, String> decode(final String name, final Map<String, String> values) {
        return values.entrySet().stream().collect(Collectors.toMap(
                entry -> String.format("%s_%s", name, entry.getKey()),
                Map.Entry::getValue
        ));
    }

}
