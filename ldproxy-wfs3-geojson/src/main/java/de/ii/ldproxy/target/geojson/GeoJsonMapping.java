/**
 * Copyright 2018 interactive instruments GmbH
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package de.ii.ldproxy.target.geojson;

import de.ii.xtraplatform.feature.query.api.TargetMapping;
import de.ii.xtraplatform.feature.transformer.api.TargetMappingProviderFromGml.GML_TYPE;

/**
 * @author zahnen
 */
public interface GeoJsonMapping extends TargetMapping<GeoJsonMapping.GEO_JSON_TYPE> {

    enum GEO_JSON_TYPE {

        ID(GML_TYPE.ID),
        STRING(GML_TYPE.STRING, GML_TYPE.DATE, GML_TYPE.DATE_TIME, GML_TYPE.URI),
        NUMBER(GML_TYPE.INT, GML_TYPE.INTEGER, GML_TYPE.LONG, GML_TYPE.SHORT, GML_TYPE.DECIMAL, GML_TYPE.DOUBLE, GML_TYPE.FLOAT),
        GEOMETRY(),
        BOOLEAN(GML_TYPE.BOOLEAN),
        NONE(GML_TYPE.NONE);

        private GML_TYPE[] gmlTypes;

        GEO_JSON_TYPE(GML_TYPE... gmlType) {
            this.gmlTypes = gmlType;
        }

        public static GEO_JSON_TYPE forGmlType(GML_TYPE gmlType) {
            for (GEO_JSON_TYPE geoJsonType : GEO_JSON_TYPE.values()) {
                for (GML_TYPE v2: geoJsonType.gmlTypes) {
                    if (v2 == gmlType) {
                        return geoJsonType;
                    }
                }
            }

            return NONE;
        }

        public boolean isValid() {
            return this != NONE;
        }
    }
}
