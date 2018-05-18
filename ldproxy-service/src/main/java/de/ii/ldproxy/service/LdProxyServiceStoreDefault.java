/**
 * Copyright 2017 European Union, interactive instruments GmbH
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package de.ii.ldproxy.service;

import com.fasterxml.aalto.stax.InputFactoryImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ii.ldproxy.codelists.CodelistStore;
import de.ii.xsf.configstore.api.KeyNotFoundException;
import de.ii.xsf.configstore.api.KeyValueStore;
import de.ii.xsf.configstore.api.rest.AbstractGenericResourceStore;
import de.ii.xsf.configstore.api.rest.ResourceTransaction;
import de.ii.xsf.core.api.Service;
import de.ii.xsf.core.util.json.DeepUpdater;
import de.ii.xsf.dropwizard.api.HttpClients;
import de.ii.xsf.dropwizard.api.Jackson;
import de.ii.xtraplatform.crs.api.CrsTransformation;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.http.client.HttpClient;
import org.codehaus.staxmate.SMInputFactory;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author zahnen
 */

// TODO: could we have a generic ServiceStore with SubStores? That would enforce id uniqueness as well.
@Component
@Provides
@Instantiate
public class LdProxyServiceStoreDefault extends AbstractGenericResourceStore<LdProxyService, LdProxyServiceStore> implements LdProxyServiceStore {

    public static final String SERVICE_STORE_ID = "ldproxy-services";

    // TODO: move to more generic WfsProxyServiceStore
    private HttpClient httpClient;
    private HttpClient sslHttpClient;
    private SMInputFactory staxFactory;
    private ObjectMapper jsonMapper;
    private CrsTransformation crsTransformation;
    private LdProxyIndexStore indexStore;
    private SparqlAdapter sparqlAdapter;
    private ScheduledExecutorService analyzerQueue;
    private  CodelistStore codelistStore;


    public LdProxyServiceStoreDefault(@Requires Jackson jackson, @Requires KeyValueStore rootConfigStore, @Requires HttpClients httpClients, @Requires CrsTransformation crsTransformation, @Requires LdProxyIndexStore indexStore, @Requires CodelistStore codelistStore) {
        super(rootConfigStore, SERVICE_STORE_ID, true, new DeepUpdater<LdProxyService>(jackson.getDefaultObjectMapper()), new LdProxyServiceSerializer(jackson.getDefaultObjectMapper()));

        jsonMapper = jackson.getDefaultObjectMapper();

        staxFactory = new SMInputFactory(new InputFactoryImpl());

        httpClient = httpClients.getDefaultHttpClient();
        sslHttpClient = httpClients.getUntrustedSslHttpClient("wfsssl");

        this.crsTransformation = crsTransformation;

        this.indexStore = indexStore;
        this.sparqlAdapter = new SparqlAdapter(jackson, httpClients);
        this.codelistStore = codelistStore;

        this.analyzerQueue = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            }
        });



        // TODO: orgs layertemplates
        //this.layerTemplateStore = new WFS2GSFSLayerTemplateStore(configStores.getConfigStore(LAYER_TEMPLATES_STORE_ID), jsonMapper);
        //this.layerTemplateStores = new HashMap<String, WFS2GSFSLayerTemplateStore>();

        //this.WFS2GSFSlayerTemplateStore = new WFS2GSFSLayerTemplateStore(this.jsonMapper, layerTemplateStore);
    }

    // TODO: needs DynamicTypeIdResolver, require does not work
    @Validate
    private void start() {
        //fillCache();
    }

    @Override
    protected LdProxyService createEmptyResource() {
        return new LdProxyService();
    }

    // TODO: move to more generic ServiceStore
    @Override
    protected LdProxyService readResource(String[] path, String id, LdProxyService resource) throws IOException, KeyNotFoundException {
        LdProxyService service = super.readResource(path, id, resource);
        service.initialize(path, httpClient, sslHttpClient, staxFactory, jsonMapper, crsTransformation);
        service.initialize(indexStore, sparqlAdapter, codelistStore);

        if (service.getTargetStatus() == Service.STATUS.STARTED) {
            service.start();
        }

        return service;
    }

    // TODO: move to more generic ServiceStore
    @Override
    protected void writeResource(String[] path, String resourceId, ResourceTransaction.OPERATION operation, LdProxyService resource) throws IOException {
        // caution: for update operations, "resource" contains only the changes, not the actual service
        LdProxyService service = resource;

        DateTime now = new DateTime();

        /*switch (operation) {
            case DELETE:
                service = getResource(path, resourceId);
                service.stop();
                break;
            case ADD:
                resource.setDateCreated(now.getMillis());
            case UPDATE:
            case UPDATE_OVERRIDE:
                resource.setLastModified(now.getMillis());
                break;
        }*/
        switch (operation) {
            case UPDATE:
            case UPDATE_OVERRIDE:
                resource.setLastModified(now.getMillis());

            case DELETE:
                // resource is not a complete instance, get the one from cache
                service = this.getResource(path, resourceId);
                service.stop();
                break;
            case ADD:
                resource.setDateCreated(now.getMillis());
        }

        if (operation == ResourceTransaction.OPERATION.UPDATE_OVERRIDE && !resource.getServiceProperties().getMappingStatus().isEnabled()) {
            resource.setServiceProperties(service.getServiceProperties());
        }
if (!(operation == ResourceTransaction.OPERATION.UPDATE_OVERRIDE && resource.getServiceProperties().getMappingStatus().isEnabled() && resource.getServiceProperties().getMappingStatus().isLoading())) {
    super.writeResource(path, resourceId, operation, resource);
}

        if (operation != ResourceTransaction.OPERATION.DELETE) {
            service = getResource(path, resourceId);
        }

        switch (operation) {
            case ADD:
                service.initialize(indexStore, sparqlAdapter, codelistStore);
            case UPDATE:
            case UPDATE_OVERRIDE:
                // also call on update for defaultCrs axis order changes
                service.initialize(path, httpClient, sslHttpClient, staxFactory, jsonMapper, crsTransformation);
                if (service.getTargetStatus() == Service.STATUS.STARTED) {
                    service.start();
                }
                if (resource.getServiceProperties().getMappingStatus().isEnabled() && resource.getServiceProperties().getMappingStatus().isLoading() && operation != ResourceTransaction.OPERATION.ADD) {
                    service.setServiceProperties(resource.getServiceProperties());
                    service.analyzeFeatureTypes();
                    //resource.setFeatureTypes(service.getFeatureTypes());
                    //resource.setServiceProperties(service.getServiceProperties());
                    //super.writeResource(path, resourceId, operation, resource);
                    super.writeResource(path, resourceId, ResourceTransaction.OPERATION.UPDATE, service);
                }
        }
    }

    @Override
    public LdProxyService addService(String id, String wfsUrl) throws IOException {
        return addService(id, wfsUrl, true);
    }

    @Override
    public LdProxyService addService(String id, String wfsUrl, boolean disableMapping) throws IOException {
        //LOGGER.info(FrameworkMessages.ADDING_WFS2GSFS_SERVICE_WITH_ID_ID_WFSURL_URL, id, queryParams.get("wfsUrl"));

        //String wfsUrl = null;
        //try {
        // TODO: what does cleanWfsUrl do? move to wfs client?
        //wfsUrl = cleanWfsUrl(queryParams.get("wfsUrl"));
        //wfsUrl = queryParams.get("wfsUrl");
        //} catch (URISyntaxException ex) {
        // TODO
        //LOGGER.error(FrameworkMessages.FAILED_REQUESTING_URL, queryParams.get("wfsUrl"));
        //throw new InvalidParameterValue(FrameworkMessages.FAILED_REQUESTING_URL, queryParams.get("wfsUrl"));
        //}

        LdProxyService service = new LdProxyService(id, wfsUrl);
        service.initialize(null, httpClient, sslHttpClient, staxFactory, jsonMapper, crsTransformation);

        service.analyzeWFS();

        addResource(service);

        if (!disableMapping) {
            queueAnalyzeService(service);
        }
        // TODO
        //serviceAddedPublisher.sendData(srvc);

        //LOGGER.info(FrameworkMessages.CREATED_WFS2GSFS_SERVICE_WITH_ID_ID_WFSURL_URL, id, queryParams.get("wfsUrl"));

        return service;
    }

    private void queueAnalyzeService(final LdProxyService service) {
        analyzerQueue.schedule(new Runnable() {
            @Override
            public void run() {
                //service.analyzeFeatureTypes();
                //LdProxyService resource = createEmptyResource();
                //resource.getServiceProperties().getMappingStatus().setEnabled(true);
                //resource.getServiceProperties().getMappingStatus().setLoading(true);
                service.getServiceProperties().getMappingStatus().setEnabled(true);
                service.getServiceProperties().getMappingStatus().setLoading(true);

                try {
                    // TODO: update ignores some properties
                    LdProxyServiceStoreDefault.this.updateResource(service);
                    //LdProxyServiceStoreDefault.this.updateResourceOverrides(service.getResourceId(), resource);
                } catch (IOException e) {
                    // TODO
                }

            }
        }, 1, TimeUnit.SECONDS);
    }
}
