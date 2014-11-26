//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
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

package org.fao.geonet.services.metadata.format;

import com.google.common.collect.Maps;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.server.dispatchers.guiservices.XmlFile;
import org.apache.commons.io.FileUtils;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.services.metadata.format.groovy.ParamValue;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.servlet.http.HttpServletRequest;

/**
 * Allows a user to display a metadata with a particular formatters
 *
 * @author jeichar
 */
@Controller("metadata.formatter")
public class Format extends AbstractFormatService {

    @Autowired
    private SettingManager settingManager;
    @Autowired
    private MetadataRepository metadataRepository;
    @Autowired
    private XmlSerializer xmlSerializer;
    @Autowired
    private XsltFormatter xsltFormatter;
    @Autowired
    private GroovyFormatter groovyFormatter;
    @Autowired
    private ServiceManager serviceManager;
    @Autowired
    private SchemaManager schemaManager;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private GeonetworkDataDirectory geonetworkDataDirectory;

    /**
     * Map (canonical path to formatter dir -> Element containing all xml files in Formatter bundle's loc directory)
     */
    private WeakHashMap<String, Element> pluginLocs = new WeakHashMap<String, Element>();


    @RequestMapping(value = "/{lang}/metadata.formatter.{type}", produces = {
            MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_HTML_VALUE })
    @ResponseBody
    public String exec(
            @PathVariable final String lang,
            @PathVariable final String type,
            @RequestParam(required = false) final String id,
            @RequestParam(required = false) final String uuid,
            @RequestParam(value = "xsl", required = false) final String xslid,
            @RequestParam(defaultValue = "n") final String skipPopularity,
            @RequestParam(value = "hide_withheld", required = false) final Boolean hide_withheld,
            final HttpServletRequest request) throws Exception {

        ServiceContext context = this.serviceManager.createServiceContext("metadata.formatter" + type, lang, request);
        Element metadata = getMetadata(context, id, uuid, new ParamValue(skipPopularity), hide_withheld);

        final String schema = schemaManager.autodetectSchema(metadata, null);
        File schemaDir = null;
        if (schema != null) {
            schemaDir = new File(schemaManager.getSchemaDir(schema));
        }
        File formatDir = getAndVerifyFormatDir(geonetworkDataDirectory, "xsl", xslid, schemaDir);

        ConfigFile config = new ConfigFile(formatDir);

        if (!isCompatibleMetadata(schema, config)) {
            throw new IllegalArgumentException("The bundle cannot format metadata with the " + schema + " schema");
        }

        File viewXslFile = new File(formatDir, FormatterConstants.VIEW_XSL_FILENAME);
        File viewGroovyFile = new File(formatDir, FormatterConstants.VIEW_GROOVY_FILENAME);


        FormatterParams fparams = new FormatterParams();
        fparams.config = config;
        fparams.format = this;
        fparams.params = request.getParameterMap();
        fparams.context = context;
        fparams.formatDir = formatDir.getCanonicalFile();
        fparams.metadata = metadata;
        fparams.schema = schema;
        fparams.url = settingManager.getSiteURL(lang);

        if (viewXslFile.exists()) {
            fparams.viewFile = viewXslFile.getCanonicalFile();
            return Xml.getString(this.xsltFormatter.format(fparams));
        } else if (viewGroovyFile.exists()){
            fparams.viewFile = viewGroovyFile.getCanonicalFile();
            return this.groovyFormatter.format(fparams);
        } else {
            throw new IllegalArgumentException("The 'xsl' parameter must be a valid id of a formatter");
        }

    }

    public Element getMetadata(ServiceContext context, String id, String uuid, ParamValue skipPopularity, Boolean hide_withheld)
            throws Exception {

        Metadata md = null;
        if (id != null) {
            try {
                md = metadataRepository.findOne(Integer.parseInt(id));
            } catch (NumberFormatException e) {
                md = metadataRepository.findOneByUuid(id);
            }
        }

        if (id == null && uuid != null) {
            md = metadataRepository.findOneByUuid(uuid);
            id = md.getId() + "";
        }

        if (md == null) {
            throw new IllegalArgumentException("Either '" + Params.UUID + "' or '" + Params.ID + "'is a required parameter");
        }
        Element metadata = xmlSerializer.removeHiddenElements(false, md);


        boolean withholdWithheldElements = hide_withheld != null && hide_withheld;
        if (XmlSerializer.getThreadLocal(false) != null || withholdWithheldElements) {
            XmlSerializer.getThreadLocal(true).setForceFilterEditOperation(withholdWithheldElements);
        }
        if (md == null) {
            throw new MetadataNotFoundEx("Metadata not found.");
        }

        Lib.resource.checkPrivilege(context, id, ReservedOperation.view);

        if (!skipPopularity.toBool()) {
            this.dataManager.increasePopularity(context, id);
        }


        return metadata;

    }
    private boolean isCompatibleMetadata(String schemaName, ConfigFile config) throws Exception {

        List<String> applicable = config.listOfApplicableSchemas();
        return applicable.contains(schemaName) || applicable.contains("all");
    }

    Element getStrings(String appPath, String lang) throws IOException, JDOMException {
        File baseLoc = new File(appPath, "loc");
        File locDir = findLocDir(lang, baseLoc);
        if (locDir.exists()) {
            return Xml.loadFile(new File(locDir, "xml" + File.separator + "strings.xml"));
        }
        return new Element("strings");
    }

    /**
     * Get the localization files from current format plugin.  It will load all xml file in the loc/lang/ directory as children
     * of the returned element.
     */
    protected synchronized Element getPluginLocResources(ServiceContext context, File formatDir, String lang) throws Exception {
        final String formatDirPath = formatDir.getPath();
        Element resources = this.pluginLocs.get(formatDirPath);
        if (isDevMode(context) || resources == null) {
            resources = new Element("loc");
            File baseLoc = new File(formatDir, "loc");
            File locDir = findLocDir(lang, baseLoc);

            resources.addContent(new Element("iso639_2").setAttribute("codeLength", "3").setText(locDir.getName()));
            String iso639_1 = context.getBean(IsoLanguagesMapper.class).iso639_2_to_iso639_1(locDir.getName());

            resources.addContent(new Element("iso639_1").setAttribute("codeLength", "2").setText(iso639_1));

            if (locDir.exists()) {
                Collection<File> files = FileUtils.listFiles(locDir, new String[]{"xml"}, false);
                for (File file : files) {
                    resources.addContent(Xml.loadFile(file));
                }
            }
            this.pluginLocs.put(formatDirPath, resources);
        }
        return resources;
    }

    private File findLocDir(String lang, File baseLoc) {
        File locDir = new File(baseLoc, lang);
        if (!locDir.exists()) {
            locDir = new File(baseLoc, Geonet.DEFAULT_LANGUAGE);
        }
        if (!locDir.exists()) {
            File[] files = baseLoc.listFiles();
            if (files != null && files.length > 0 && files[0].isDirectory()) {
                locDir = files[0];
            }
        }
        return locDir;
    }

    /**
     * Get the strings.xml, codelists.xml and labels.xml for the correct language from the schema plugin
     *
     * @return Map(SchemaName, SchemaLocalizations)
     */
    protected Map<String, SchemaLocalization> getSchemaLocalizations(ServiceContext context)
            throws IOException, JDOMException {

        Map<String, SchemaLocalization> localization =  Maps.newHashMap();
        final SchemaManager schemaManager = context.getBean(SchemaManager.class);
        final Set<String> allSchemas = schemaManager.getSchemas();
        for (String schema : allSchemas) {
            Map<String, XmlFile> schemaInfo = schemaManager.getSchemaInfo(schema);
            localization.put(schema, new SchemaLocalization(context, schema, schemaInfo));
        }

        return localization;
    }

    protected boolean isDevMode(ServiceContext context) {
        return context.getApplicationContext().getBean(SystemInfo.class).isDevMode();
    }

}
