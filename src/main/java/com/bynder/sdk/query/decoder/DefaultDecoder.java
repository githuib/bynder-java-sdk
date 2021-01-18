package com.bynder.sdk.query.decoder;

import java.util.HashMap;
import java.util.Map;

public class DefaultDecoder implements ParameterDecoder<Object> {

    @Override
    public Map<String, String> decode(final String name, final Object value) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(name, value.toString());
        return parameters;
    }

}
