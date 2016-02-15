package org.fao.geonet.solr;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.model.language.ConstantExpression;
import org.apache.camel.model.language.SimpleExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.io.IOException;

/**
 */
public class CatalogSynchronizer extends RouteBuilder {
    static ApplicationContext applicationContext;

    private DataSource dataSource;

    @Autowired
    private Utility xslUtility;

    @Autowired
    private SolrConfig solrConfig;

    private int checkEvery = 10000;
//    public static void main(String[] args) throws IOException {
//        applicationContext = new ClassPathXmlApplicationContext("config-spring-geonetwork.xml");
//        synchDatabaseWithSolr();
//    }


    @Override
    public void configure() throws Exception {

        try {
            SimpleRegistry registry = new SimpleRegistry() ;
            registry.put("sourceDataSource", dataSource);
            registry.put("indexerState", IndexerState.getInstance());
            registry.put("xsltUtility", xslUtility);

            CamelContext context = new DefaultCamelContext(registry);


            RouteBuilder indexDocumentRoute = new RouteBuilder() {
                public void configure() {
                    from("direct:indexDocument")
                        .to("bean:xsltUtility?method=transform(*, '/xslt/metadata.xsl')")
                        .setHeader(Exchange.HTTP_URI, new SimpleExpression(
                                solrConfig.getSolrServerUrl() + "/" +
                                solrConfig.getSolrServerCore() + "/update?commit=true"))
                        .setHeader("camelHttpMethod", new ConstantExpression("POST"))
                        .setHeader("Content-Type", new ConstantExpression("text/xml"))
                        .to("http4://solr.server.url?throwExceptionOnFailure=false")
                        .log(LoggingLevel.DEBUG,
                                "org.geonetwork.solr.synchronizer",
                                "Solr response: ${body}");
                }
            };


            RouteBuilder indexAllRecordsRoute = new RouteBuilder() {
                public void configure() {
                    from("quartz2://myGroup/myTimerName?trigger.repeatInterval=1&trigger.repeatCount=10000")
                        .setHeader("lastChangeDate").method("indexerState", "getLastUpdate")
                        .log(LoggingLevel.INFO,
                                "org.geonetwork.solr.synchronizer",
                                "Last update ${header.lastChangeDate}.")
//                            .to("sql:SELECT id, uuid, data, isharvested FROM metadata WHERE id = 277 ORDER BY changedate desc?dataSourceRef=sourceDataSource")
                        // WHERE schemaid = 'iso19139'
                        .to("sql:SELECT id, uuid, data, isharvested, changedate FROM metadata ORDER BY changedate desc?dataSourceRef=sourceDataSource")
                        .log(LoggingLevel.INFO,
                                "org.geonetwork.solr.synchronizer",
                                "Found ${headers.CamelSqlRowCount} records to index.")
                        .split()
                            .simple("${body}")
                            .to("bean:indexerState?method=setLastUpdate")
                            .log(LoggingLevel.INFO,
                                    "org.geonetwork.solr.synchronizer",
                                    "Last update is now: ${headers.lastChangeDate}.")
                            .setBody().simple("${body[data]}")
//                            .log(LoggingLevel.INFO,
//                                    "org.geonetwork.solr.synchronizer",
//                                    "XML: ${body}")
                            .to("direct:indexDocument");
                }
            };




            RouteBuilder indexUpdatedRecordsRoute = new RouteBuilder() {
                public void configure() {
                    from("quartz2://index/check-for-updates?trigger.repeatInterval=" + checkEvery)
                            .setHeader("lastChangeDate").method("indexerState", "getLastUpdate")
                            .log(LoggingLevel.INFO,
                                    "org.geonetwork.solr.synchronizer",
                                    "Checking number of records updated since ${header.lastChangeDate}.")
                            .to("sql:SELECT id, uuid, data, isharvested, changedate FROM metadata WHERE changedate > :#lastChangeDate ORDER BY changedate desc")
                            //?dataSourceRef=sourceDataSource
                            .log(LoggingLevel.INFO,
                                    "org.geonetwork.solr.synchronizer",
                                    "Found ${headers.CamelSqlRowCount} records to be indexed.")
                            .split()
                            .simple("${body}")
                            .to("bean:indexerState?method=setLastUpdate")
                            .log(LoggingLevel.INFO,
                                    "org.geonetwork.solr.synchronizer",
                                    "Last update is now: ${headers.lastChangeDate}.")
                            .setBody().simple("${body[data]}")
                            .to("direct:indexDocument");
                }
            };



            context.addRoutes(indexDocumentRoute);
            context.addRoutes(indexAllRecordsRoute);
            context.addRoutes(indexUpdatedRecordsRoute);
            context.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int getCheckEvery() {
        return checkEvery;
    }

    public void setCheckEvery(int checkEvery) {
        this.checkEvery = checkEvery;
    }
}
