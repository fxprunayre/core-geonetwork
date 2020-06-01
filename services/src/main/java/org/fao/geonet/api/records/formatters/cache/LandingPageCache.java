/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.api.records.formatters.cache;

import jeeves.constants.Jeeves;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.records.formatters.FormatType;
import org.fao.geonet.api.records.formatters.FormatterApi;
import org.fao.geonet.api.records.formatters.FormatterWidth;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.ServletContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LandingPageCache {

    private String landingPageFormatter;
    private Map<String, String> landingPageFormatterParameters = new HashMap<>();
    private String landingPageLanguage = Geonet.DEFAULT_LANGUAGE;
    private ServiceContext _context;

    public void setLandingPageFormatter(String landingPageFormatter) {
        if (landingPageFormatter.contains("?")) {
            final String[] strings = landingPageFormatter.split("\\?");
            this.landingPageFormatter = strings[0];
            if (strings.length > 1) {
                for (String param : strings[1].split("&")) {
                    if(param.contains("=")) {
                        final String[] paramAndValue = param.split("=");
                        this.landingPageFormatterParameters.put(paramAndValue[0], paramAndValue[1]);
                    } else {
                        this.landingPageFormatterParameters.put(param, "");
                    }
                }
            }
        } else {
            this.landingPageFormatter = landingPageFormatter;
        }
    }

    public void setContext(ServiceContext context) {
        this._context = context;
    }

    public void setLandingPageLanguage(String landingPageLanguage) {
        this.landingPageLanguage = landingPageLanguage;
    }

    @Autowired
    OperationAllowedRepository operationAllowedRepo;
    @Autowired
    IMetadataUtils metadataUtils;
    @Autowired
    MetadataRepository metadataRepository;
    @Autowired
    FormatterApi formatService;

    /**
     * Fill the cache of landing pages.
     *
     * @param context
     */
    public void fillLandingPageCache(final ServiceContext context) {
        Thread fillCaches = new Thread(new Runnable() {
            @Override
            public void run() {
                final ServletContext servletContext = context.getServlet().getServletContext();
                context.setAsThreadLocal();

                final List<Integer> allPublicRecordIds = operationAllowedRepo.findAllPublicRecordIds();

                final MockHttpSession servletSession = new MockHttpSession(servletContext);
                servletSession.setAttribute(Jeeves.Elem.SESSION, context.getUserSession());
                final MockHttpServletRequest servletRequest = new MockHttpServletRequest(servletContext);
                servletRequest.setSession(servletSession);
                servletRequest.setParameters(landingPageFormatterParameters);
                final MockHttpServletResponse response = new MockHttpServletResponse();

                allPublicRecordIds.stream().forEach(r -> {
                    try {
                        formatService.getRecordFormattedBy(
                            MediaType.TEXT_HTML_VALUE,
                            landingPageFormatter,
                            metadataUtils.getMetadataUuid(r + ""),
                            FormatterWidth._100,
                            null,
                            landingPageLanguage,
                            FormatType.html,
                            true,
                            true,
                            false,
                            new ServletWebRequest(servletRequest, response),
                            servletRequest);
                    } catch (Throwable t) {
                        Log.info(Geonet.GEONETWORK, String.format(
                            "Error building the landing page with formatter '%s' for record '%s'.",
                            landingPageFormatter, r), t);
                    }
                });
            }
        });
        fillCaches.setDaemon(true);
        fillCaches.setName("Fill formatter cache thread");
        fillCaches.setPriority(Thread.MIN_PRIORITY);
        fillCaches.start();
    }

    /**
     * If a landing page formatter is defined, build the landing page
     * by calling the formatter service. Landing page are only computed
     * on public records (ie. no template, no private records).
     * @param metadataId
     */
    public void buildLandingPage(int metadataId) {
        if (_context == null || StringUtils.isEmpty(landingPageFormatter)) {
            return;
        }

        final ServletContext servletContext = _context.getServlet().getServletContext();
        _context.setAsThreadLocal();
        final MockHttpSession servletSession = new MockHttpSession(servletContext);
        servletSession.setAttribute(Jeeves.Elem.SESSION, _context.getUserSession());
        final MockHttpServletRequest servletRequest = new MockHttpServletRequest(servletContext);
        servletRequest.setSession(servletSession);
        servletRequest.setParameters(landingPageFormatterParameters);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        
        try {
            formatService.getRecordFormattedBy(
                MediaType.TEXT_HTML_VALUE,
                landingPageFormatter,
                metadataUtils.getMetadataUuid(metadataId + ""),
                FormatterWidth._100,
                null,
                landingPageLanguage,
                FormatType.html,
                true,
                true,
                true,
                new ServletWebRequest(servletRequest, response),
                servletRequest);
        } catch (Throwable t) {
            Log.info(Geonet.GEONETWORK, String.format(
                "Error building the landing page with formatter '%s' for record '%s'.",
                landingPageFormatter, metadataId), t);
        }
    }
}
