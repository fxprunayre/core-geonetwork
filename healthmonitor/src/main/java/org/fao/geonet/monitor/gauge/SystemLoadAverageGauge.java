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

package org.fao.geonet.monitor.gauge;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import jeeves.monitor.MetricsFactory;
import jeeves.server.context.ServiceContext;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Abstract super class for all Gauges that use the Main database Stats.
 *
 * User: jeichar Date: 4/5/12 Time: 10:09 AM
 */
public class SystemLoadAverageGauge implements MetricsFactory<Gauge<Double>> {

    public Gauge<Double> create(MetricRegistry metricsRegistry, final ServiceContext context) {
        return metricsRegistry.register(
            name(OperatingSystemMXBean.class, "systemLoadAverage"),
            new Gauge<Double>() {
                @Override
                public Double getValue() {
                    OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
                    return operatingSystemMXBean.getSystemLoadAverage();
                }
        });
    }


}
