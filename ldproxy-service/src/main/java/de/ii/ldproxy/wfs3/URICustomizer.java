/**
 * Copyright 2018 interactive instruments GmbH
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package de.ii.ldproxy.wfs3;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zahnen
 */
public class URICustomizer extends URIBuilder {
    public URICustomizer() {
        super();
    }

    public URICustomizer(String string) throws URISyntaxException {
        super(string);
    }

    public URICustomizer(URI uri) {
        super(uri);
    }

    public URICustomizer copy() {
        try {
            return new URICustomizer(this.toString());
        } catch (URISyntaxException e) {
            //ignore
        }
        return this;
    }

    @Override
    public URICustomizer clearParameters() {
        super.clearParameters();
        return this;
    }

    public URICustomizer ensureParameter(final String parameter, final String value) {
        if (this.getQueryParams()
                .stream()
                .noneMatch(nameValuePair -> nameValuePair.getName()
                                                         .equals(parameter))) {
            this.setParameter(parameter, value);
        }
        return this;
    }

    public URICustomizer removeParameters(final String... parameters) {
        final List<String> parameterList = Arrays.asList(parameters);
        final List<NameValuePair> queryParams = this.getQueryParams()
                                                    .stream()
                                                    .filter(nameValuePair -> !parameterList.contains(nameValuePair.getName()))
                                                    .collect(Collectors.toList());

        this.setParameters(queryParams);

        return this;
    }

    public boolean isLastPathSegment(final String segment) {
        final List<String> pathSegments = getPathSegments();

        return !pathSegments.isEmpty() && pathSegments.get(pathSegments.size() - 1)
                                                    .equals(segment);
    }

    public URICustomizer ensureLastPathSegment(final String segment) {
        final List<String> pathSegments = getPathSegments();

        if (!pathSegments.isEmpty() && !pathSegments.get(pathSegments.size() - 1)
                                                    .equals(segment)) {
            this.setPathSegments(new ImmutableList.Builder<String>()
                    .addAll(pathSegments)
                    .add(segment)
                    .build());
        }
        return this;
    }

    public URICustomizer ensureLastPathSegments(final String... segments) {
        final List<String> pathSegments = getPathSegments();

        int pathSegmentsIndex = pathSegments.size() >= segments.length ? pathSegments.size() - segments.length : 0;

        boolean match = true;
        int restIndex = 0;
        for (int k = pathSegmentsIndex; k < pathSegments.size(); k++) {
            for (int l = 0; k + l < pathSegments.size(); l++) {
                if (!pathSegments.get(k + l)
                                 .equals(segments[l])) {
                    match = false;
                    break;
                }
                restIndex = l + 1;
            }
            if (match) {
                break;
            } else {
                match = true;
            }
        }

        int segmentsIndex = match ? restIndex : 0;

        this.setPathSegments(new ImmutableList.Builder<String>()
                .addAll(pathSegments)
                .addAll(Arrays.asList(segments)
                              .subList(segmentsIndex, segments.length))
                .build());

        return this;
    }

    public URICustomizer removePathSegment(final String segment, final int index) {
        final List<String> pathSegments = getPathSegments();
        final int i = index < 0 ? pathSegments.size() + index : index;

        if (i >= 0 && i < pathSegments.size() && pathSegments.get(i).equals(segment)) {
                this.setPathSegments(new ImmutableList.Builder<String>()
                        .addAll(pathSegments.subList(0, i))
                        .addAll(pathSegments.subList(i+1, pathSegments.size()))
                        .build());
        }
        return this;
    }

    public URICustomizer removeLastPathSegment(final String segment) {
        final List<String> pathSegments = getPathSegments();

        if (!pathSegments.isEmpty() && pathSegments.get(pathSegments.size() - 1)
                                                   .equals(segment)) {
            this.setPathSegments(pathSegments.subList(0, pathSegments.size() - 1));
        }
        return this;
    }

    public URICustomizer removeLastPathSegments(final int numberOfSegments) {
        final List<String> pathSegments = getPathSegments();

        if (pathSegments.size() >= numberOfSegments) {
            this.setPathSegments(pathSegments.subList(0, pathSegments.size() - numberOfSegments));
        }

        return this;
    }

    private List<String> getPathSegments() {
        return Splitter.on('/')
                       .omitEmptyStrings()
                       .splitToList(this.getPath());
    }

    private void setPathSegments(final List<String> pathSegments) {
        this.setPath(Joiner.on('/')
                           .join(pathSegments));
    }
}
