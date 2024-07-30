//=============================================================================
//===	Copyright (C) 2001-2011 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.datamanager.base;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.client.ClientHttpResponse;


public class GeoNetwork5MetadataIndexer implements IMetadataIndexer, ApplicationEventPublisherAware {

    private IMetadataUtils metadataUtils;
    private String baseUrl;

    public GeoNetwork5MetadataIndexer() {
    }

    public void setupIndex()
        throws Exception {
        HttpGet getMethod = new HttpGet(baseUrl  + "/setup");
        try (ClientHttpResponse httpResponse = executeRequest(getMethod)) {
            int status = httpResponse.getRawStatusCode();
            Log.debug(Geonet.DATA_MANAGER, String.format(
                "GeoNetwork 5 created index. Status is %d.", status));
        }
    }

    public void init(ServiceContext context, Boolean force) throws Exception {
        this.requestFactory = ApplicationContextHolder.get().getBean(GeonetHttpRequestFactory.class);
    }

    @Override
    public void setMetadataUtils(IMetadataUtils metadataUtils) {
        this.metadataUtils = metadataUtils;
    }

    @Override
    public void setMetadataManager(IMetadataManager metadataManager) {
    }

    @Override
    public void forceIndexChanges() throws IOException {
    }

    @Override
    public int batchDeleteMetadataAndUpdateIndex(Specification<? extends AbstractMetadata> specification)
        throws Exception {
        return 0;
    }

    /**
     * Reindex all records in current selection.
     */
    @Override
    public synchronized void rebuildIndexForSelection(final ServiceContext context, String bucket, boolean clearXlink)
        throws Exception {

        // get all metadata ids from selection
        ArrayList<String> listOfIdsToIndex = new ArrayList<String>();
        UserSession session = context.getUserSession();
        SelectionManager sm = SelectionManager.getManager(session);

        synchronized (sm.getSelection(bucket)) {
            for (Iterator<String> iter = sm.getSelection(bucket).iterator(); iter.hasNext(); ) {
                String uuid = iter.next();
                String id = metadataUtils.getMetadataId(uuid);
                if (id != null) {
                    listOfIdsToIndex.add(id);
                }
            }
        }

        if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
            Log.debug(Geonet.DATA_MANAGER, "Will index " + listOfIdsToIndex.size() + " records from selection.");
        }

        if (!listOfIdsToIndex.isEmpty()) {
            batchIndexInThreadPool(context, listOfIdsToIndex);
        }
    }

    @Override
    public void batchIndexInThreadPool(ServiceContext context, List<?> metadataIds) {
        StringBuilder metadataIdsBuilder = new StringBuilder();

        try {
            HttpGet getMethod = new HttpGet(baseUrl + "/index" + (metadataIds.isEmpty()
                ? ""
                : "?uuid=" + Joiner.on("&uuid=")
                .appendTo(metadataIdsBuilder,
                    ((List<String>)metadataIds).stream().map(id -> {
                        try {
                            return metadataUtils.getMetadataUuid(id);
                        } catch (Exception e) {
                            return null;
                        }
                    }).collect(Collectors.toUnmodifiableList()))));
            try (ClientHttpResponse httpResponse = executeRequest(getMethod)) {
                int status = httpResponse.getRawStatusCode();
                Log.debug(Geonet.DATA_MANAGER, String.format(
                    "GeoNetwork 5 indexing started for %d record(s). Status is %d.", metadataIds.size(), status));
            }
        } catch (Exception e) {
            Log.error(Geonet.DATA_MANAGER, "Error while indexing metadata.", e);
        }
    }

    @Override
    public boolean isIndexing() {
        return false;
    }

    @Override
    public void indexMetadata(final List<String> metadataIds) throws Exception {
        for (String metadataId : metadataIds) {
            indexMetadata(metadataId, true, IndexingMode.full);
        }
    }

    @Override
    public void indexMetadata(final String metadataId,
                              final boolean forceRefreshReaders,
                              final IndexingMode indexingMode)
        throws Exception {
        String metadataUuid = metadataUtils.getMetadataUuid(metadataId);
        HttpGet getMethod = new HttpGet(baseUrl + "/index?uuid=" + metadataUuid);
        try (ClientHttpResponse httpResponse = executeRequest(getMethod)) {
            int status = httpResponse.getRawStatusCode();
            Log.debug(Geonet.DATA_MANAGER, String.format(
                "GeoNetwork 5 indexing started for %s. Status is %d.", metadataUuid, status));
        }
    }


    @Override
    public void indexMetadataPrivileges(String uuid, int id) throws Exception {

    }

    @Override
    public void versionMetadata(ServiceContext context, String id, Element md) throws Exception {
    }

    protected GeonetHttpRequestFactory requestFactory;

    protected ClientHttpResponse executeRequest(HttpUriRequest method) throws Exception {
        final String requestHost = method.getURI().getHost();

        final Function<HttpClientBuilder, Void> requestConfiguration = new Function<HttpClientBuilder, Void>() {
            @Nullable
            @Override
            public Void apply(@Nonnull HttpClientBuilder input) {
//                final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//                credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
//                input.setDefaultCredentialsProvider(credentialsProvider);
                Lib.net.setupProxy(ApplicationContextHolder.get().getBean(SettingManager.class), input, requestHost);
                input.useSystemProperties();
                return null;
            }
        };

        try {
            return requestFactory.execute(method, requestConfiguration);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {

    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
