package com.bynder.sdk.query.decoder;

import com.bynder.sdk.query.MetapropertyAttribute;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MetapropertyAttributesDecoder implements ParameterDecoder<List<MetapropertyAttribute>> {

    @Override
    public Map<String, String> decode(final String name, final List<MetapropertyAttribute> value) {
        return value.stream().collect(Collectors.toMap(
                metapropertyAttribute -> String.format("%s.%s", name, metapropertyAttribute.getMetapropertyId()),
                metapropertyAttribute -> String.join(",", metapropertyAttribute.getOptionsIds())
        ));
    }
}
