package org.fao.geonet.api.records.editing;

import javassist.NotFoundException;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class InspireValidatorUtilsTest extends AbstractServiceIntegrationTest {

    @Autowired
    SettingManager settingManager;

    private static String URL = "http://inspire-sandbox.jrc.ec.europa.eu/etf-webapp";

    @Test
    public void testGetReportUrl() {

        String reportUrl = InspireValidatorUtils.getReportUrl(URL, "123");

        assertEquals(URL + "/v2/TestRuns/123.html", reportUrl);
    }

    @Test
    public void testGetReportUrlJSON() {

        String reportUrl = InspireValidatorUtils.getReportUrlJSON(URL, "123");

        assertEquals(URL + "/v2/TestRuns/123.json", reportUrl);
    }

    @Test
    @Ignore
    public void testLifeCycle() {

        assertEquals(InspireValidatorUtils.checkServiceStatus("http://wrong.url.eu", null, settingManager), false);

        // FIRST TEST IF OFFICIAL ETF IS AVAILABLE
        // Needed to avoid GN errors when ETF is not available
        if (InspireValidatorUtils.checkServiceStatus(URL, null, settingManager)) {

            try {
                // No file
                InspireValidatorUtils.submitFile(URL, null, "Metadata (TG version 1.3)", "GN UNIT TEST ", settingManager);
            } catch (IllegalArgumentException e) {
                // RIGHT EXCEPTION
            } catch (Exception e) {
                assertEquals("Wrong exception.", "IllegalArgumentException", "Exception");
            }

            try {
                // Valid but not found test ID
                InspireValidatorUtils.isReady(URL, "IED123456789012345678901234567890123", null, settingManager);
                assertEquals("No exception!", "NotFoundException", "No Exception");
            } catch (NotFoundException e) {
                // RIGHT EXCEPTION
            } catch (Exception e) {
                assertEquals("Wrong exception.", "NotFoundException", "Exception");
            }

            try {
                // Test ID in wrong format
                assertEquals(InspireValidatorUtils.isPassed(URL, "1", null, settingManager), null);
            } catch (Exception e) {
                assertEquals("Unexpected exception.", "Exception", "No Exception");
            }

        } else {
            assertEquals("The official ETF endpoint is not available. Can't run further tests.", URL, URL);
        }

    }


}

