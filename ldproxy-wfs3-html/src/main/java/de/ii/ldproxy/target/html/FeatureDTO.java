/**
 * Copyright 2018 interactive instruments GmbH
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package de.ii.ldproxy.target.html;

/**
 * @author zahnen
 */
public class FeatureDTO extends FeaturePropertyDTO {
    public FeaturePropertyDTO id;
    //public final List<FeaturePropertyDTO> properties;
    public FeaturePropertyDTO geo;
    public FeaturePropertyDTO links;
    public boolean idAsUrl;
    public boolean noUrlClosingSlash;

    /*public FeatureDTO() {
        this.properties = new ArrayList<>();
    }*/


}
