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

package jeeves.monitor;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import com.codahale.metrics.core.DummyCounter;
import com.codahale.metrics.core.DummyHistogram;
import com.codahale.metrics.core.DummyMeter;
import com.codahale.metrics.core.DummyTimer;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.log4j2.InstrumentedAppender;
import jeeves.constants.ConfigFile;
import jeeves.server.context.ServiceContext;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.fao.geonet.Util;
import org.fao.geonet.utils.Log;
import org.jdom.Element;

import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static jeeves.constants.ConfigFile.Monitors.Child.*;

/**
 * Contains references to the monitor factories to start for each App
 *
 * User: jeichar Date: 3/29/12 Time: 3:42 PM
 */
public class MonitorManager {
    public static final String HEALTH_CHECK_REGISTRY = "io.dropwizard.metrics.reporting.HealthCheckServlet.registry";
    public static final String CRITICAL_HEALTH_CHECK_REGISTRY = "io.dropwizard.metrics.reporting.HealthCheckServlet.registry.critical";
    public static final String WARNING_HEALTH_CHECK_REGISTRY = "io.dropwizard.metrics.reporting.HealthCheckServlet.registry.warning";
    public static final String EXPENSIVE_HEALTH_CHECK_REGISTRY = "io.dropwizard.metrics.reporting.HealthCheckServlet.registry.expensive";
    public static final String METRICS_REGISTRY = "io.dropwizard.metrics.reporting.MetricsServlet.registry";
    private final List<HealthCheckFactory> criticalServiceContextHealthChecks = new LinkedList<HealthCheckFactory>();
    private final List<HealthCheckFactory> warningServiceContextHealthChecks = new LinkedList<HealthCheckFactory>();
    private final List<HealthCheckFactory> expensiveServiceContextHealthChecks = new LinkedList<HealthCheckFactory>();
    private final Map<Class<MetricsFactory<Gauge<?>>>, Gauge<?>> serviceContextGauges = new HashMap<Class<MetricsFactory<Gauge<?>>>, Gauge<?>>();
    private final Map<Class<MetricsFactory<Timer>>, Timer> serviceContextTimers = new HashMap<Class<MetricsFactory<Timer>>, Timer>();
    private final Map<Class<MetricsFactory<Counter>>, Counter> serviceContextCounters = new HashMap<Class<MetricsFactory<Counter>>, Counter>();
    private final Map<Class<MetricsFactory<Histogram>>, Histogram> serviceContextHistogram = new HashMap<Class<MetricsFactory<Histogram>>, Histogram>();
    private final Map<Class<MetricsFactory<Meter>>, Meter> serviceContextMeter = new HashMap<Class<MetricsFactory<Meter>>, Meter>();
    ResourceTracker resourceTracker = new ResourceTracker();
    private HealthCheckRegistry healthCheckRegistry;
    private HealthCheckRegistry criticalHealthCheckRegistry;
    private HealthCheckRegistry warningHealthCheckRegistry;
    private HealthCheckRegistry expensiveHealthCheckRegistry;

    private MetricRegistry metricsRegistry;
    private JmxReporter jmxReporter;

    public void init(ServletContext context, String baseUrl) {

        String webappName = baseUrl.substring(1);

        if (context != null) {
            HealthCheckRegistry tmpHealthCheckRegistry = lookUpHealthCheckRegistry(context, HEALTH_CHECK_REGISTRY);
            HealthCheckRegistry criticalTmpHealthCheckRegistry = lookUpHealthCheckRegistry(context, CRITICAL_HEALTH_CHECK_REGISTRY);
            HealthCheckRegistry warningTmpHealthCheckRegistry = lookUpHealthCheckRegistry(context, WARNING_HEALTH_CHECK_REGISTRY);
            HealthCheckRegistry expensiveTmpHealthCheckRegistry = lookUpHealthCheckRegistry(context, EXPENSIVE_HEALTH_CHECK_REGISTRY);

            healthCheckRegistry = tmpHealthCheckRegistry;
            criticalHealthCheckRegistry = criticalTmpHealthCheckRegistry;
            warningHealthCheckRegistry = warningTmpHealthCheckRegistry;
            expensiveHealthCheckRegistry = expensiveTmpHealthCheckRegistry;

            MetricRegistry tmpMetricsRegistry = (MetricRegistry) context.getAttribute(METRICS_REGISTRY);
            if (tmpMetricsRegistry == null) {
                tmpMetricsRegistry = new MetricRegistry();
            }

            metricsRegistry = tmpMetricsRegistry;
            context.setAttribute(METRICS_REGISTRY, tmpMetricsRegistry);
            jmxReporter = JmxReporter.forRegistry(metricsRegistry).inDomain(webappName).build();
            jmxReporter.start();
        } else {
            healthCheckRegistry = new HealthCheckRegistry();
            criticalHealthCheckRegistry = new HealthCheckRegistry();
            warningHealthCheckRegistry = new HealthCheckRegistry();
            expensiveHealthCheckRegistry = new HealthCheckRegistry();
            metricsRegistry = new MetricRegistry();

            jmxReporter = JmxReporter.forRegistry(metricsRegistry).inDomain(webappName).build();
            jmxReporter.start();

        }
        Filter filter = null;        // That's fine if we don't use filters; https://logging.apache.org/log4j/2.x/manual/filters.html
        PatternLayout layout = null; // The layout isn't used in InstrumentedAppender
        InstrumentedAppender appender =
            new InstrumentedAppender(metricsRegistry, filter, layout, false);
        appender.start();

        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        Configuration config = loggerContext.getConfiguration();
        config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME)
            .addAppender(appender, Level.WARN, filter);
        loggerContext.updateLoggers(config);
    }

    private HealthCheckRegistry lookUpHealthCheckRegistry(ServletContext context, String attributeKey) {
        HealthCheckRegistry tmpHealthCheckRegistry = (HealthCheckRegistry) context.getAttribute(attributeKey);
        if (tmpHealthCheckRegistry == null) {
            tmpHealthCheckRegistry = new HealthCheckRegistry();
        }
        context.setAttribute(attributeKey, tmpHealthCheckRegistry);
        return tmpHealthCheckRegistry;
    }

    public void initMonitorsForApp(ServiceContext context) {
        createHealthCheck(context, criticalServiceContextHealthChecks, criticalHealthCheckRegistry, "critical health check");
        createHealthCheck(context, warningServiceContextHealthChecks, warningHealthCheckRegistry, "warning health check");
        createHealthCheck(context, expensiveServiceContextHealthChecks, expensiveHealthCheckRegistry, "expensive health check");

        for (Class<MetricsFactory<Gauge<?>>> factoryClass : serviceContextGauges.keySet()) {
            Log.info(Log.ENGINE, "Instantiating : " + factoryClass.getName());
            Gauge<?> instance = create(factoryClass, context, SERVICE_CONTEXT_GAUGE);
            serviceContextGauges.put(factoryClass, instance);
        }
        for (Class<MetricsFactory<Timer>> factoryClass : serviceContextTimers.keySet()) {
            Log.info(Log.ENGINE, "Instantiating : " + factoryClass.getName());
            Timer instance = create(factoryClass, context, SERVICE_CONTEXT_TIMER);
            serviceContextTimers.put(factoryClass, instance);
        }
        for (Class<MetricsFactory<Counter>> factoryClass : serviceContextCounters.keySet()) {
            Log.info(Log.ENGINE, "Instantiating : " + factoryClass.getName());
            Counter instance = create(factoryClass, context, SERVICE_CONTEXT_COUNTER);
            serviceContextCounters.put(factoryClass, instance);
        }
        for (Class<MetricsFactory<Histogram>> factoryClass : serviceContextHistogram.keySet()) {
            Log.info(Log.ENGINE, "Instantiating : " + factoryClass.getName());
            Histogram instance = create(factoryClass, context, SERVICE_CONTEXT_HISTOGRAM);
            serviceContextHistogram.put(factoryClass, instance);
        }
        for (Class<MetricsFactory<Meter>> factoryClass : serviceContextMeter.keySet()) {
            Log.info(Log.ENGINE, "Instantiating : " + factoryClass.getName());
            Meter instance = create(factoryClass, context, SERVICE_CONTEXT_METER);
            serviceContextMeter.put(factoryClass, instance);
        }
    }

    private void createHealthCheck(ServiceContext context, List<HealthCheckFactory> checks, HealthCheckRegistry registry, String type) {
        for (HealthCheckFactory healthCheck : checks) {
            Log.info(Log.ENGINE, "Registering " + type + ": " + healthCheck.getClass().getName());
            HealthCheck check = healthCheck.create(context);
            healthCheckRegistry.register(context.getNodeId(), check);
            registry.register(context.getNodeId(), check);
        }
    }

    private <T> T create(Class<MetricsFactory<T>> factoryClass, ServiceContext context, String type) {
        try {
            MetricsFactory<T> instance = factoryClass.newInstance();
            return instance.create(metricsRegistry, context);
        } catch (Exception e) {
            logReflectionError(e, factoryClass.getName(), type);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public void initMonitors(Element monitors) {
        info("Initializing monitors...");

        //--- get schedules root package
        String pack = monitors.getAttributeValue(ConfigFile.Monitors.Attr.PACKAGE);

        // --- scan serviceContextHealthCheck elements
        initHealthChecks(monitors, pack, CRITICAL_SERVICE_CONTEXT_HEALTH_CHECK, criticalServiceContextHealthChecks);
        initHealthChecks(monitors, pack, WARNING_SERVICE_CONTEXT_HEALTH_CHECK, warningServiceContextHealthChecks);
        initHealthChecks(monitors, pack, EXPENSIVE_SERVICE_CONTEXT_HEALTH_CHECK, expensiveServiceContextHealthChecks);

        for (Element gauge : (List<Element>) monitors.getChildren(SERVICE_CONTEXT_GAUGE)) {
            serviceContextGauges.put(this.<MetricsFactory<Gauge<?>>>loadClass(gauge, pack, SERVICE_CONTEXT_GAUGE), null);
        }
        serviceContextGauges.remove(null);
        for (Element gauge : (List<Element>) monitors.getChildren(SERVICE_CONTEXT_COUNTER)) {
            serviceContextCounters.put(this.<MetricsFactory<Counter>>loadClass(gauge, pack, SERVICE_CONTEXT_GAUGE), null);
        }
        serviceContextCounters.remove(null);
        for (Element gauge : (List<Element>) monitors.getChildren(SERVICE_CONTEXT_TIMER)) {
            serviceContextTimers.put(this.<MetricsFactory<Timer>>loadClass(gauge, pack, SERVICE_CONTEXT_TIMER), null);
        }
        serviceContextTimers.remove(null);
        for (Element gauge : (List<Element>) monitors.getChildren(SERVICE_CONTEXT_HISTOGRAM)) {
            serviceContextHistogram.put(this.<MetricsFactory<Histogram>>loadClass(gauge, pack, SERVICE_CONTEXT_HISTOGRAM), null);
        }
        serviceContextHistogram.remove(null);
        for (Element gauge : (List<Element>) monitors.getChildren(SERVICE_CONTEXT_METER)) {
            serviceContextMeter.put(this.<MetricsFactory<Meter>>loadClass(gauge, pack, SERVICE_CONTEXT_METER), null);
        }
        serviceContextMeter.remove(null);
    }

    @SuppressWarnings("unchecked")
    private void initHealthChecks(Element monitors, String pack, String tagName, List<HealthCheckFactory> checkCollection) {
        for (Element check : (List<Element>) monitors.getChildren(tagName)) {
            Class<HealthCheckFactory> hcClass = loadClass(check, pack, tagName);
            try {
                checkCollection.add(hcClass.newInstance());
            } catch (Exception e) {
                logReflectionError(e, hcClass != null ? hcClass.getName() : "unknown", tagName);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> loadClass(Element monitor, String pack, String type) {

        String name = monitor.getAttributeValue(ConfigFile.Monitors.Attr.CLASS);

        info("   Adding " + type + ": " + name);

        String className = name;
        if (name.startsWith(".")) {
            className = pack + name;
        }
        try {
            return (Class<T>) Class.forName(className);
        } catch (Exception e) {
            logReflectionError(e, className, type);
            return null;
        }
    }

    private void logReflectionError(Exception e, String className, String type) {
        error("Raised exception while registering " + type + ". Skipped.");
        error("   Class name  : " + className);
        error("   Exception : " + e);
        error("   Message   : " + e.getMessage());
        error("   Stack     : " + Util.getStackTrace(e));
    }


    private void info(String s) {
        Log.info(Log.MONITOR, s);
    }

    private void error(String s) {
        Log.error(Log.MONITOR, s);
    }

    public Counter getCounter(Class<? extends MetricsFactory<Counter>> type) {
        Counter instance = serviceContextCounters.get(type);
        if (instance == null) {
            return DummyCounter.INSTANCE;
        } else {
            return instance;
        }
    }

    public Timer getTimer(Class<? extends MetricsFactory<Timer>> type) {
        Timer instance = serviceContextTimers.get(type);
        if (instance == null) {
            return DummyTimer.INSTANCE;
        } else {
            return instance;
        }
    }

    public Histogram getHistogram(Class<? extends MetricsFactory<Histogram>> type) {
        Histogram instance = serviceContextHistogram.get(type);
        if (instance == null) {
            return DummyHistogram.INSTANCE;
        } else {
            return instance;
        }
    }

    public Meter getMeter(Class<? extends MetricsFactory<Meter>> type) {
        Meter instance = serviceContextMeter.get(type);
        if (instance == null) {
            return DummyMeter.INSTANCE;
        } else {
            return instance;
        }
    }

    public ResourceTracker getResourceTracker() {
        return resourceTracker;
    }

    @PreDestroy
    public void shutdown() {
        Log.info(Log.ENGINE, "MonitorManager#shutdown");
        if (resourceTracker != null) {
            resourceTracker.clean();
        }
        if (jmxReporter != null) {
            jmxReporter.close();
        }
    }
}
