package org.fao.geonet.solr;

import org.apache.camel.Exchange;

import java.util.Map;

/**
 * Created by francois on 30/06/15.
 */
public class IndexerState {
    private static IndexerState INSTANCE = new IndexerState();
    public static IndexerState getInstance() {
        return INSTANCE;
    }

    private String lastUpdate;

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Exchange exchange) {
        exchange.getOut().setBody(exchange.getIn().getBody());
        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
        Map body = (Map)exchange.getOut().getBody();
        String lastUpdate = (String)body.get("changedate");

        if (this.lastUpdate == null) {
           this.lastUpdate = lastUpdate;
        } else {
            if (lastUpdate.compareTo(this.lastUpdate) > 0) {
                System.out.println(lastUpdate + " vs " + this.lastUpdate + " : " +
                        lastUpdate.compareTo(this.lastUpdate));
                this.lastUpdate = lastUpdate;
            }
        }
        exchange.getOut().setHeader("lastChangeDate", this.lastUpdate);
        System.out.println(this + " has " + this.lastUpdate);
    }
}
