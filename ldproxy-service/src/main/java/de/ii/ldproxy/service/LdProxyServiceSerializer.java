/**
 * Copyright 2017 European Union, interactive instruments GmbH
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package de.ii.ldproxy.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.ii.xsf.configstore.api.rest.GenericResourceSerializer;
import de.ii.xsf.core.api.JsonViews;

import java.io.IOException;

/**
 * @author zahnen
 */
public class LdProxyServiceSerializer extends GenericResourceSerializer<LdProxyService> {
    private final ObjectMapper updateJsonMapper;

    public LdProxyServiceSerializer(ObjectMapper jsonMapper) {
        super(jsonMapper);
        updateJsonMapper = jsonMapper.copy();
        updateJsonMapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
    }

    @Override
    public String serializeAdd(LdProxyService resource) throws IOException {
        return jsonMapper
                .writerWithView(JsonViews.FullView.class)
                .writeValueAsString(resource);
    }

    @Override
    public String serializeUpdate(LdProxyService resource) throws IOException {
        return updateJsonMapper
                .writerWithView(JsonViews.ConfigurationView.class)
                .without(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS)
                .writeValueAsString(resource);
    }
}

