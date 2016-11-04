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

package org.fao.geonet.monitor.webapp;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * {@link Filter} implementation which captures request information and a breakdown of the response
 * codes being returned.
 */
public abstract class WebappMetricsFilter implements Filter {
    private final String otherMetricName;
    private final Map<Integer, String> meterNamesByStatusCode;
    private final String registryAttribute;

    // initialized after call of init method
    private ConcurrentMap<Integer, Meter> metersByStatusCode;
    private Meter otherMeter;
    private Counter activeRequests;
    private Timer requestTimer;


    /**
     * Creates a new instance of the filter.
     *
     * @param registryAttribute      the attribute used to look up the metrics registry in the
     *                               servlet context
     * @param meterNamesByStatusCode A map, keyed by status code, of meter names that we are
     *                               interested in.
     * @param otherMetricName        The name used for the catch-all meter.
     */
    public WebappMetricsFilter(String registryAttribute, Map<Integer, String> meterNamesByStatusCode,
                               String otherMetricName) {
        this.registryAttribute = registryAttribute;
        this.otherMetricName = otherMetricName;
        this.meterNamesByStatusCode = meterNamesByStatusCode;
    }


    public void init(FilterConfig filterConfig) throws ServletException {
        final MetricRegistry metricsRegistry = getMetricsFactory(filterConfig);

        this.metersByStatusCode = new ConcurrentHashMap<Integer, Meter>(meterNamesByStatusCode
            .size());
        for (Entry<Integer, String> entry : meterNamesByStatusCode.entrySet()) {
            metersByStatusCode.put(entry.getKey(),
                metricsRegistry.meter(name(
                    WebappMetricsFilter.class,
                    entry.getValue())));
        }
        this.otherMeter = metricsRegistry.meter(name(
            WebappMetricsFilter.class,
            otherMetricName));
        this.activeRequests = metricsRegistry.counter(name(
            WebappMetricsFilter.class, "activeRequests"));
        this.requestTimer = metricsRegistry.timer(name(WebappMetricsFilter.class,
            "requests"));

    }

    private MetricRegistry getMetricsFactory(FilterConfig filterConfig) {
        final MetricRegistry metricsRegistry;

        final Object o = filterConfig.getServletContext().getAttribute(this.registryAttribute);
        if (o instanceof MetricRegistry) {
            metricsRegistry = (MetricRegistry) o;
        } else {
            metricsRegistry = new MetricRegistry();
        }
        return metricsRegistry;
    }

    public void destroy() {
//        Metrics.defaultRegistry().shutdown();
    }

    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        final StatusExposingServletResponse wrappedResponse =
            new StatusExposingServletResponse((HttpServletResponse) response);
        activeRequests.inc();
        final Timer.Context context = requestTimer.time();
        try {
            chain.doFilter(request, wrappedResponse);
        } finally {
            context.stop();
            activeRequests.dec();
            markMeterForStatusCode(wrappedResponse.getStatus());
        }
    }

    private void markMeterForStatusCode(int status) {
        final Meter metric = metersByStatusCode.get(status);
        if (metric != null) {
            metric.mark();
        } else {
            otherMeter.mark();
        }
    }

    private static class StatusExposingServletResponse extends HttpServletResponseWrapper {
        private int httpStatus;

        public StatusExposingServletResponse(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void sendError(int sc) throws IOException {
            httpStatus = sc;
            super.sendError(sc);
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            httpStatus = sc;
            super.sendError(sc, msg);
        }

        public int getStatus() {
            return httpStatus;
        }

        @Override
        public void setStatus(int sc) {
            httpStatus = sc;
            super.setStatus(sc);
        }
    }
}
