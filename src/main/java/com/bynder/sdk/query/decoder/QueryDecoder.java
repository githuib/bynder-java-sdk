/*
 * Copyright (c) 2019 Bynder B.V. All rights reserved.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license
 * information.
 */
package com.bynder.sdk.query.decoder;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Decodes query object to a Map of parameters.
 */
public class QueryDecoder {

    /**
     * Method called for each field in a query object. It extracts the different fields with
     * {@link ApiField} annotation and, if needed, calls appropriate converter to convert
     * property value to string.
     *
     * @param field Field information.
     * @param query Query object.
     * @param parameters Parameters name/value pairs to send to the API.
     * @throws IllegalAccessException If the Field object is inaccessible.
     */
    private static void convertField(
            final Field field,
            final Object query,
            final Map<String, String> parameters
    ) throws IllegalAccessException, InstantiationException {
        field.setAccessible(true);

        ApiField apiField = field.getAnnotation(ApiField.class);
        Object fieldValue = field.get(query);
        if (apiField != null && fieldValue != null) {
            String name = ApiField.DEFAULT_NAME.equals(apiField.name())
                    ? field.getName()
                    : apiField.name();

            ParameterDecoder decoder = apiField.decoder().newInstance();
            parameters.putAll(decoder.decode(name, fieldValue));
        }

        field.setAccessible(false);
    }

    /**
     * Given a query object this method gets its API parameters. The parameters are basically the
     * fields of the query object that have {@link ApiField} annotation.
     *
     * @param query Query object.
     * @return Map with parameters name/value pairs to send to the API.
     */
    public Map<String, String> decode(final Object query) {
        Map<String, String> parameters = new HashMap<>();
        if (query != null) {
            List<Field> fields = new ArrayList<>(Arrays.asList(query.getClass().getDeclaredFields()));
            fields.addAll(Arrays.asList(query.getClass().getSuperclass().getDeclaredFields()));
            for (Field field : fields) {
                try {
                    convertField(field, query, parameters);
                } catch (IllegalAccessException | InstantiationException e) {
                    // Nothing to do. This will never happen as
                    // we are iterating over the fields.
                }
            }
        }
        return parameters;
    }
}
