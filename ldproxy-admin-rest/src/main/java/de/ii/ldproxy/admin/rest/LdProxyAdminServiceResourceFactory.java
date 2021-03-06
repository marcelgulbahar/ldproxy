/**
 * Copyright 2018 interactive instruments GmbH
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package de.ii.ldproxy.admin.rest;

import de.ii.xtraplatform.service.api.AdminServiceResource;
import de.ii.xtraplatform.service.api.AdminServiceResourceFactory;
import de.ii.xtraplatform.service.api.ServiceResource;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;

/**
 *
 * @author zahnen
 */
@Component
@Provides(properties= {
            @StaticServiceProperty(name=ServiceResource.SERVICE_TYPE_KEY, type="java.lang.String", value= "WFS3")
    })
@Instantiate

public class LdProxyAdminServiceResourceFactory implements AdminServiceResourceFactory {

    @Override
    public Class getAdminServiceResourceClass() {
        return LdProxyAdminServiceResource.class;
    }

    @Override
    public AdminServiceResource getAdminServiceResource() {
        return new LdProxyAdminServiceResource();
    }

}
