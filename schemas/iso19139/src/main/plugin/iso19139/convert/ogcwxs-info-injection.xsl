<?xml version="1.0" encoding="UTF-8"?>

<!--
  ~ Copyright (C) 2001-2016 Food and Agriculture Organization of the
  ~ United Nations (FAO-UN), United Nations World Food Programme (WFP)
  ~ and United Nations Environment Programme (UNEP)
  ~
  ~ This program is free software; you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation; either version 2 of the License, or (at
  ~ your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful, but
  ~ WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
  ~
  ~ Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
  ~ Rome - Italy. email: geonetwork@osgeo.org
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:wfs="http://www.opengis.net/wfs"
                xmlns:ows="http://www.opengis.net/ows"
                xmlns:owsg="http://www.opengeospatial.net/ows"
                xmlns:ows11="http://www.opengis.net/ows/1.1"
                xmlns:wcs="http://www.opengis.net/wcs"
                xmlns:wms="http://www.opengis.net/wms"
                xmlns:wps="http://www.opengeospatial.net/wps"
                xmlns:wps1="http://www.opengis.net/wps/1.0.0"
                xmlns:inspire_vs="http://inspire.ec.europa.eu/schemas/inspire_vs/1.0"
                xmlns:inspire_common="http://inspire.ec.europa.eu/schemas/common/1.0"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:math="http://exslt.org/math"
                extension-element-prefixes="saxon math"
                exclude-result-prefixes="#all"
                version="2.0">

  <xsl:import href="OGCWxSGetCapabilitiesto19119/resp-party.xsl"/>

  <xsl:output indent="yes"/>

  <!-- Metadata record UUID generated. -->
  <xsl:param name="uuid"
             select="''"/>


  <xsl:variable name="record"
                select="/root/record"/>
  <xsl:variable name="getCapabilities"
                select="/root/getCapabilities"/>
  <xsl:variable name="rootName"
                select="$getCapabilities/*/local-name()"/>
  <xsl:variable name="ows">
    <xsl:choose>
      <xsl:when test="($rootName='WFS_Capabilities' and namespace-uri($getCapabilities/*)='http://www.opengis.net/wfs' and $getCapabilities/*/@version='1.1.0')
          or ($rootName='Capabilities' and namespace-uri($getCapabilities/*)='http://www.opengeospatial.net/wps')
          or ($rootName='Capabilities' and namespace-uri($getCapabilities/*)='http://www.opengis.net/wps/1.0.0')">
        true
      </xsl:when>
      <xsl:otherwise>false</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <!-- Define mapping between GetCapabilities document and
  where to insert information in the template used.

  The target element MUST exist in the template.
  The matching value in the GetCapabilities document replace the matching element.
  If the matching element is text, the value is inserted in the attribute or element.
  If the matching element is an element, a template is used to convert the element
  to the corresponding element.

  target attribute MUST be a full Xpath without search clause ie. [...].
  source attribute is the matching element in the GetCapabilities document.
  mode: insert (as a child of target)|after (target)
  name: customize name of the generated element. Useful for contact which may have different
  element names in the standard depending on the location.

  TODO: Add a check of all none matching element.
  TODO: Handle updates to not insert things twice.
  -->
  <xsl:variable name="properties">
    <!-- Insert title. -->
    <element target="/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"
             source="/(*/ows:ServiceIdentification/ows:Title|
                     */ows11:ServiceIdentification/ows11:Title|
                     */wfs:Service/wfs:Title|
                     */wms:Service/wms:Title|
                     */Service/Title|
                     */wcs:Service/wcs:label)/text()"/>

    <!-- Insert title. -->
    <element target="/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:abstract/gco:CharacterString"
             source="/(*/ows:ServiceIdentification/ows:Abstract|
                     */ows11:ServiceIdentification/ows11:Abstract|
                     */wfs:Service/wfs:Abstract|
                     */wms:Service/wms:Abstract|
                     */Service/Abstract|
                     */wcs:Service/wcs:description)/text()"/>

    <!-- Insert keywords. -->
    <element target="/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:descriptiveKeywords"
             source="/(*/ows:ServiceIdentification/ows:Keywords|
                     */ows11:ServiceIdentification/ows11:Keywords|
                     */wms:Service/wms:KeywordList|
                     */wfs:Service/wfs:keywords|
                     */Service/KeywordList|
                     */wcs:Service/wcs:keywords|
                     *//inspire_vs:ExtendedCapabilities/inspire_common:MandatoryKeyword)"
              mode="after"/>


    <!-- Insert language. -->
    <!-- TODO: Get from harvester parameters?-->
    <element target="/gmd:MD_Metadata/gmd:language/gmd:LanguageCode/@codeListValue"
             source="//inspire_vs:ExtendedCapabilities/inspire_common:ResponseLanguage/inspire_common:Language/text()"
             default="eng"/>

    <!-- Match gmd:contact and replace it by the corresponding contact information.
    Note: gmd:contact MUST be part of the template. -->
    <element target="/gmd:MD_Metadata/gmd:contact"
             source="/(*/Service/ContactInformation|
                      */wfs:Service/wfs:ContactInformation|
                      */wms:Service/wms:ContactInformation|
                      */ows:ServiceProvider|
                      */owsg:ServiceProvider|
                      */ows11:ServiceProvider)"
             name="gmd:contact"/>
    <element target="/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:pointOfContact"
             source="/(*//ContactInformation|
                       *//wcs:responsibleParty|
                       *//wms:responsibleParty|
                       *//wms:Service/wms:ContactInformation|
                       *//ows:ServiceProvider|
                       *//ows11:ServiceProvider)"
             name="gmd:pointOfContact"/>



    <element target="/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:resourceConstraints"
             source="/(*//wms:AccessConstraints)"/>

    <element target="/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:accessProperties/gmd:MD_StandardOrderProcess/gmd:fees/gco:CharacterString"
             source="/(*//*:Fees)"/>

    <!-- INSPIRE extension elements -->
    <!-- Insert dateStamp or let the system set it to now. -->
    <element target="/gmd:MD_Metadata/gmd:dateStamp"
             source="//inspire_vs:ExtendedCapabilities/inspire_common:MetadataDate/text()"/>



    <!-- Insert GetCapabilities URL.
     A transfertOptions (even empty) section MUST be set. -->
    <element target="/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions"
             source="/(.//wms:GetCapabilities/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource|
                       .//wfs:GetCapabilities/wfs:DCPType/wfs:HTTP/wfs:Get|
                       .//ows:Operation[@name='GetCapabilities']/ows:DCP/ows:HTTP/ows:Get|
                       .//ows11:Operation[@name='GetCapabilities']/ows11:DCP/ows11:HTTP/ows11:Get|
                       .//GetCapabilities/DCPType/HTTP/Get/OnlineResource[1]|
                       .//wcs:GetCapabilities//wcs:OnlineResource[1])"
             mode="insert"/>

    <!-- Insert INSPIRE conformity section.
     scope is mandatory so the DQ report will be inserted after. -->
    <element target="/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:scope"
             source="//inspire_vs:ExtendedCapabilities/inspire_common:Conformity[
                            inspire_common:Degree='conformant' or
                            inspire_common:Degree='notConformant']"
             mode="after"/>


    <!-- Insert INSPIRE last revision date. -->
    <element target="/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:date"
             source="//inspire_vs:ExtendedCapabilities/inspire_common:TemporalReference/inspire_common:DateOfLastRevision"
             mode="after"/>


    <!-- Some elements are not declared here but are processed by a mode copy template
     like srv:serviceType, srv:extent, couplingType, srv:containsOperations. -->

<!-- This does not work as we need to aggregate all boxes
    <element target="/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent"
             source="/(*//ows:WGS84BoundingBox|
                       *//wcs:lonLatEnvelope|
                       *//wms:EX_GeographicBoundingBox|
                       *//wfs:LatLongBoundingBox|
                       *//LatLonBoundingBox)"/>
-->

  </xsl:variable>



  <xsl:template match="/">
    <xsl:apply-templates mode="copy-or-inject"
                         select="/root/record/*"/>
  </xsl:template>



  <xsl:template mode="copy-or-inject"
                match="gmd:fileIdentifier/gco:CharacterString/text()">
    <xsl:value-of select="$uuid"/>
  </xsl:template>



  <xsl:template mode="copy-or-inject"
                match="*|@*"
                priority="5">

    <xsl:variable name="xpath"
                  select="concat(
                            replace(
                              string-join(ancestor::*/name(), '/'),
                            'root/record', ''),
                          '/',
                          if (. instance of attribute()) then '@' else '',
                          name(.))"/>
    <xsl:variable name="match"
                  select="$properties/element[@target = $xpath]"/>
    <xsl:variable name="isMatch"
                  select="count($match) > 0"/>
    <xsl:message>* <xsl:value-of select="$xpath"/></xsl:message>

    <xsl:choose>
      <xsl:when test="$isMatch">
        <xsl:variable name="capabilitiesValue"
                      select="saxon:evaluate(concat('$p1', $match/@source), $getCapabilities)"/>

        <xsl:message> = <xsl:value-of select="$capabilitiesValue"/> </xsl:message>

        <xsl:choose>
          <!-- Make a copy of the template value when no match found. -->
          <xsl:when test="not($capabilitiesValue)">
            <xsl:apply-templates mode="copy"
                                 select="."/>
          </xsl:when>
          <!-- Inject text in attribute value -->
          <xsl:when test=". instance of attribute()">
            <xsl:attribute name="{name(.)}"
                           select="$capabilitiesValue"/>
          </xsl:when>
          <!-- Inject element or multiple values in element -->
          <xsl:when test="count($capabilitiesValue) > 1 or $capabilitiesValue instance of element()">

            <xsl:choose>
              <xsl:when test="$match/@mode = 'insert'">
                <!-- Insert source as a child after copying
                existing children of the target. -->
                <xsl:copy>
                  <xsl:apply-templates mode="copy-or-inject"
                                       select="*|@*"/>
                  <xsl:comment>Node inserted as child of <xsl:value-of select="$xpath"/>
                               from <xsl:value-of select="normalize-space($match/@source)"/>.</xsl:comment>
                  <xsl:apply-templates mode="convert"
                                       select="$capabilitiesValue"/>
                </xsl:copy>
              </xsl:when>
              <xsl:when test="$match/@mode = 'after'">
                <!-- Insert source after copying
                existing target. -->
                <xsl:copy>
                  <xsl:apply-templates mode="copy-or-inject"
                                       select="*|@*"/>
                </xsl:copy>
                <xsl:comment>Node inserted after <xsl:value-of select="$xpath"/>
                             from <xsl:value-of select="normalize-space($match/@source)"/>.</xsl:comment>
                <xsl:apply-templates mode="convert"
                                     select="$capabilitiesValue"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:comment>Value set from <xsl:value-of select="normalize-space($match/@source)"/>.</xsl:comment>
                <xsl:choose>
                  <xsl:when test="$match/@name">
                    <xsl:element name="{$match/@name}">
                      <xsl:apply-templates mode="convert"
                                           select="$capabilitiesValue"/>
                    </xsl:element>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:apply-templates mode="convert"
                                         select="$capabilitiesValue"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <!-- Inject text in element -->
          <xsl:otherwise>
            <xsl:copy>
              <xsl:comment>Value of <xsl:value-of select="$xpath"/>
                set from <xsl:value-of select="normalize-space($match/@source)"/>.</xsl:comment>
              <xsl:value-of select="$capabilitiesValue"/>
            </xsl:copy>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="copy"
                             select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>





  <xsl:template mode="convert"
                match="Service/ContactInformation|
                       wfs:Service/wfs:ContactInformation|
                       wms:Service/wms:ContactInformation|
                       wcs:responsibleParty|
                       wms:responsibleParty|
                       ows:ServiceProvider|
                       owsg:ServiceProvider|
                       ows11:ServiceProvider">
    <gmd:CI_ResponsibleParty>
      <xsl:apply-templates select="."
                           mode="RespParty"/>
    </gmd:CI_ResponsibleParty>
  </xsl:template>


  <xsl:template mode="convert"
                match="ows:Keywords|
                       ows11:Keywords|
                       wfs:keywords|
                       KeywordList|
                       wcs:keywords">
    <gmd:descriptiveKeywords>
      <gmd:MD_Keywords>
        <gmd:keyword>
          <gco:CharacterString><xsl:value-of select="."/></gco:CharacterString>
        </gmd:keyword>
      </gmd:MD_Keywords>
    </gmd:descriptiveKeywords>
  </xsl:template>



  <xsl:template mode="convert"
                match="wms:KeywordList">
    <!-- Add keyword part of a vocabulary -->
    <xsl:for-each-group select="wms:Keyword[@vocabulary]" group-by="@vocabulary">
      <gmd:descriptiveKeywords>
        <gmd:MD_Keywords>
          <xsl:for-each select="../wms:Keyword[@vocabulary = current-grouping-key()]">
            <gmd:keyword>
              <gco:CharacterString>
                <xsl:value-of select="."/>
              </gco:CharacterString>
            </gmd:keyword>
          </xsl:for-each>
          <gmd:type>
            <gmd:MD_KeywordTypeCode codeList="./resources/codeList.xml#MD_KeywordTypeCode"
                                codeListValue="theme"/>
          </gmd:type>
          <xsl:if test="current-grouping-key() != ''">
            <gmd:thesaurusName>
              <gmd:CI_Citation>
                <gmd:title>
                  <gco:CharacterString>
                    <xsl:value-of select="current-grouping-key()"/>
                  </gco:CharacterString>
                </gmd:title>
                <gmd:date gco:nilReason="missing"/>
              </gmd:CI_Citation>
            </gmd:thesaurusName>
          </xsl:if>
        </gmd:MD_Keywords>
      </gmd:descriptiveKeywords>
    </xsl:for-each-group>


    <!-- Add other WMS keywords -->
    <xsl:if test="wms:Keyword[not(@vocabulary)]">
      <gmd:descriptiveKeywords>
        <gmd:MD_Keywords>
          <xsl:for-each select="wms:Keyword[not(@vocabulary)]">
            <gmd:keyword>
              <gco:CharacterString>
                <xsl:value-of select="."/>
              </gco:CharacterString>
            </gmd:keyword>
          </xsl:for-each>
          <gmd:type>
            <gmd:MD_KeywordTypeCode codeList="./resources/codeList.xml#MD_KeywordTypeCode"
                                codeListValue="theme"/>
          </gmd:type>
        </gmd:MD_Keywords>
      </gmd:descriptiveKeywords>
    </xsl:if>
  </xsl:template>


  <xsl:template mode="convert"
                match="inspire_common:MandatoryKeyword[@xsi:type='inspire_common:classificationOfSpatialDataService']">
    <gmd:descriptiveKeywords>
      <gmd:MD_Keywords>
        <xsl:for-each select="inspire_common:KeywordValue">
          <gmd:keyword>
            <gco:CharacterString>
              <xsl:value-of select="."/>
            </gco:CharacterString>
          </gmd:keyword>
        </xsl:for-each>
        <gmd:type>
          <gmd:MD_KeywordTypeCode
            codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_KeywordTypeCode"
            codeListValue="theme"/>
        </gmd:type>
        <gmd:thesaurusName>
          <gmd:CI_Citation>
            <gmd:title>
              <gco:CharacterString>INSPIRE Service taxonomy</gco:CharacterString>
            </gmd:title>
            <gmd:date>
              <gmd:CI_Date>
                <gmd:date>
                  <gco:Date>2010-04-22</gco:Date>
                </gmd:date>
                <gmd:dateType>
                  <gmd:CI_DateTypeCode
                    codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_DateTypeCode"
                    codeListValue="publication"/>
                </gmd:dateType>
              </gmd:CI_Date>
            </gmd:date>
          </gmd:CI_Citation>
        </gmd:thesaurusName>
      </gmd:MD_Keywords>
    </gmd:descriptiveKeywords>
  </xsl:template>



  <xsl:template mode="convert"
                match="wms:AccessConstraints">
    <gmd:resourceConstraints>
      <gmd:MD_LegalConstraints>
        <xsl:choose>
          <xsl:when test=". = 'copyright'
              or . = 'patent'
              or . = 'patentPending'
              or . = 'trademark'
              or . = 'license'
              or . = 'intellectualPropertyRight'
              or . = 'restricted'
              ">
            <gmd:accessConstraints>
              <gmd:MD_RestrictionCode
                codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_RestrictionCode"
                codeListValue="{.}"/>
            </gmd:accessConstraints>
          </xsl:when>
          <xsl:when test="lower-case(.) = 'none'">
            <gmd:accessConstraints>
              <gmd:MD_RestrictionCode
                codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_RestrictionCode"
                codeListValue="otherRestrictions"/>
            </gmd:accessConstraints>
            <gmd:otherConstraints>
              <gco:CharacterString>no conditions apply</gco:CharacterString>
            </gmd:otherConstraints>
          </xsl:when>
          <xsl:otherwise>
            <gmd:accessConstraints>
              <gmd:MD_RestrictionCode
                codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_RestrictionCode"
                codeListValue="otherRestrictions"/>
            </gmd:accessConstraints>
            <gmd:otherConstraints>
              <gco:CharacterString>
                <xsl:value-of select="."/>
              </gco:CharacterString>
            </gmd:otherConstraints>
          </xsl:otherwise>
        </xsl:choose>
      </gmd:MD_LegalConstraints>
    </gmd:resourceConstraints>

    <xsl:if test="lower-case(.) = 'none'">
      <gmd:resourceConstraints>
        <gmd:MD_Constraints>
          <gmd:useLimitation>
            <gco:CharacterString>no conditions apply</gco:CharacterString>
          </gmd:useLimitation>
        </gmd:MD_Constraints>
      </gmd:resourceConstraints>
    </xsl:if>

  </xsl:template>




  <xsl:template mode="convert"
                match="wms:OnlineResource|
                       wfs:Get">
    <gmd:onLine>
      <gmd:CI_OnlineResource>
        <gmd:linkage>
          <gmd:URL><xsl:value-of select="@xlink:href|@onlineResource"/></gmd:URL>
        </gmd:linkage>
        <gmd:protocol>
          <gco:CharacterString>
            <xsl:choose>
              <xsl:when test="$rootName = ('WMT_MS_Capabilities', 'WMS_Capabilities')">OGC:WMS</xsl:when>
              <xsl:when test="$rootName = ('WFS_MS_Capabilities', 'WFS_Capabilities')">OGC:WFS</xsl:when>
              <xsl:when test="$rootName = ('WCS_Capabilities')">OGC:WCS</xsl:when>
              <xsl:when test="$rootName = ('Capabilities')">OGC:WPS</xsl:when>
              <xsl:otherwise>WWW:LINK-1.0-http--link</xsl:otherwise>
            </xsl:choose>
          </gco:CharacterString>
        </gmd:protocol>

        <gmd:description>
          <gco:CharacterString>GetCapabilities URL</gco:CharacterString>
        </gmd:description>
      </gmd:CI_OnlineResource>
    </gmd:onLine>
  </xsl:template>


  <!--
  <inspire_common:Conformity>
      <inspire_common:Specification>
          <inspire_common:Title>-</inspire_common:Title>
          <inspire_common:DateOfLastRevision>2013-01-01</inspire_common:DateOfLastRevision>
      </inspire_common:Specification>
      <inspire_common:Degree>notEvaluated</inspire_common:Degree>
  </inspire_common:Conformity>
  -->
  <xsl:template mode="convert"
                match="inspire_common:Conformity">
    <gmd:report>
      <gmd:DQ_DomainConsistency>
        <gmd:result>
          <gmd:DQ_ConformanceResult>
            <gmd:specification>
              <gmd:CI_Citation>
                <gmd:title>
                  <gco:CharacterString>
                    <xsl:value-of
                      select="inspire_common:Specification/inspire_common:Title"/>
                  </gco:CharacterString>
                </gmd:title>
                <gmd:date>
                  <gmd:CI_Date>
                    <gmd:date>
                      <gco:Date>
                        <xsl:value-of
                          select="inspire_common:Specification/inspire_common:DateOfLastRevision"/>
                      </gco:Date>
                    </gmd:date>
                    <gmd:dateType>
                      <gmd:CI_DateTypeCode
                        codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_DateTypeCode"
                        codeListValue="revision"/>
                    </gmd:dateType>
                  </gmd:CI_Date>
                </gmd:date>
              </gmd:CI_Citation>
            </gmd:specification>
            <!-- gmd:explanation is mandated by ISO 19115. A default value is proposed -->
            <gmd:explanation>
              <gco:CharacterString>See the referenced specification</gco:CharacterString>
            </gmd:explanation>
            <!-- the value is false instead of true if not conformant -->
            <xsl:choose>
              <xsl:when test="inspire_common:Degree='conformant'">
                <gmd:pass>
                  <gco:Boolean>true</gco:Boolean>
                </gmd:pass>
              </xsl:when>
              <xsl:when test="inspire_common:Degree='notConformant'">
                <gmd:pass>
                  <gco:Boolean>false</gco:Boolean>
                </gmd:pass>
              </xsl:when>
              <xsl:otherwise>
                <!-- Not evaluated -->
                <gmd:pass gco:nilReason="unknown">
                  <gco:Boolean/>
                </gmd:pass>
              </xsl:otherwise>
            </xsl:choose>

          </gmd:DQ_ConformanceResult>
        </gmd:result>
      </gmd:DQ_DomainConsistency>
    </gmd:report>
  </xsl:template>


  <xsl:template mode="convert"
                match="inspire_common:DateOfLastRevision">
    <gmd:date>
      <gmd:CI_Date>
        <gmd:date>
          <gco:Date>
            <xsl:value-of
              select="."/>
          </gco:Date>
        </gmd:date>
        <gmd:dateType>
          <gmd:CI_DateTypeCode codeList="./resources/codeList.xml#CI_DateTypeCode"
                               codeListValue="revision"/>
        </gmd:dateType>
      </gmd:CI_Date>
    </gmd:date>
  </xsl:template>




  <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
          Extent in OGC spec are somehow differents !

          WCS 1.0.0
          <lonLatEnvelope srsName="WGS84(DD)">
                  <gml:pos>-130.85168 20.7052</gml:pos>
                  <gml:pos>-62.0054 54.1141</gml:pos>
          </lonLatEnvelope>

          WFS 1.1.0
          <ows:WGS84BoundingBox>
                  <ows:LowerCorner>-124.731422 24.955967</ows:LowerCorner>
                  <ows:UpperCorner>-66.969849 49.371735</ows:UpperCorner>
          </ows:WGS84BoundingBox>

          WMS 1.1.1
          <LatLonBoundingBox minx="-74.047185" miny="40.679648" maxx="-73.907005" maxy="40.882078"/>

          WMS 1.3.0
          <EX_GeographicBoundingBox>
              <westBoundLongitude>-178.9988054730254</westBoundLongitude>
              <eastBoundLongitude>179.0724773329789</eastBoundLongitude>
              <southBoundLatitude>-0.5014529001680404</southBoundLatitude>
              <northBoundLatitude>88.9987992292308</northBoundLatitude>
          </EX_GeographicBoundingBox>
          <BoundingBox CRS="EPSG:4326" minx="27.116136375774644" miny="-17.934116876940887" maxx="44.39484823803499" maxy="6.052081516030762"/>

          WPS 0.4.0 : none

          WPS 1.0.0 : none
           -->
  <xsl:template mode="copy"
                match="srv:extent">
    <srv:extent>
      <gmd:EX_Extent>
        <gmd:geographicElement>
          <gmd:EX_GeographicBoundingBox>

            <xsl:choose>
              <xsl:when test="$ows='true' or $rootName='WCS_Capabilities'">

                <xsl:variable name="boxes">
                  <xsl:choose>
                    <xsl:when test="$ows='true'">
                      <xsl:for-each select="$getCapabilities//ows:WGS84BoundingBox/ows:LowerCorner">
                        <xmin>
                          <xsl:value-of select="substring-before(., ' ')"/>
                        </xmin>
                        <ymin>
                          <xsl:value-of select="substring-after(., ' ')"/>
                        </ymin>
                      </xsl:for-each>
                      <xsl:for-each select="$getCapabilities//ows:WGS84BoundingBox/ows:UpperCorner">
                        <xmax>
                          <xsl:value-of select="substring-before(., ' ')"/>
                        </xmax>
                        <ymax>
                          <xsl:value-of select="substring-after(., ' ')"/>
                        </ymax>
                      </xsl:for-each>
                    </xsl:when>
                    <xsl:when test="$rootName='WCS_Capabilities'">
                      <xsl:for-each select="$getCapabilities//wcs:lonLatEnvelope/gml:pos[1]">
                        <xmin>
                          <xsl:value-of select="substring-before(., ' ')"/>
                        </xmin>
                        <ymin>
                          <xsl:value-of select="substring-after(., ' ')"/>
                        </ymin>
                      </xsl:for-each>
                      <xsl:for-each select="$getCapabilities//wcs:lonLatEnvelope/gml:pos[2]">
                        <xmax>
                          <xsl:value-of select="substring-before(., ' ')"/>
                        </xmax>
                        <ymax>
                          <xsl:value-of select="substring-after(., ' ')"/>
                        </ymax>
                      </xsl:for-each>
                    </xsl:when>
                  </xsl:choose>
                </xsl:variable>


                <gmd:westBoundLongitude>
                  <gco:Decimal>
                    <xsl:value-of select="math:min($boxes/*[name(.)='xmin'])"/>
                  </gco:Decimal>
                </gmd:westBoundLongitude>
                <gmd:eastBoundLongitude>
                  <gco:Decimal>
                    <xsl:value-of select="math:max($boxes/*[name(.)='xmax'])"/>
                  </gco:Decimal>
                </gmd:eastBoundLongitude>
                <gmd:southBoundLatitude>
                  <gco:Decimal>
                    <xsl:value-of select="math:min($boxes/*[name(.)='ymin'])"/>
                  </gco:Decimal>
                </gmd:southBoundLatitude>
                <gmd:northBoundLatitude>
                  <gco:Decimal>
                    <xsl:value-of select="math:max($boxes/*[name(.)='ymax'])"/>
                  </gco:Decimal>
                </gmd:northBoundLatitude>

              </xsl:when>
              <xsl:otherwise>

                <gmd:westBoundLongitude>
                  <gco:Decimal>
                    <xsl:value-of
                      select="math:min($getCapabilities//(wms:westBoundLongitude|//LatLonBoundingBox/@minx|//wfs:LatLongBoundingBox/@minx))"/>
                  </gco:Decimal>
                </gmd:westBoundLongitude>
                <gmd:eastBoundLongitude>
                  <gco:Decimal>
                    <xsl:value-of
                      select="math:max($getCapabilities//(wms:eastBoundLongitude|//LatLonBoundingBox/@maxx|//wfs:LatLongBoundingBox/@maxx))"/>
                  </gco:Decimal>
                </gmd:eastBoundLongitude>
                <gmd:southBoundLatitude>
                  <gco:Decimal>
                    <xsl:value-of
                      select="math:min($getCapabilities//(wms:southBoundLatitude|//LatLonBoundingBox/@miny|//wfs:LatLongBoundingBox/@miny))"/>
                  </gco:Decimal>
                </gmd:southBoundLatitude>
                <gmd:northBoundLatitude>
                  <gco:Decimal>
                    <xsl:value-of
                      select="math:max($getCapabilities//(wms:northBoundLatitude|//LatLonBoundingBox/@maxy|//wfs:LatLongBoundingBox/@maxy))"/>
                  </gco:Decimal>
                </gmd:northBoundLatitude>
              </xsl:otherwise>
            </xsl:choose>


          </gmd:EX_GeographicBoundingBox>
        </gmd:geographicElement>
      </gmd:EX_Extent>
    </srv:extent>
  </xsl:template>




  <xsl:template mode="copy-or-inject"
                match="srv:serviceType">

    <srv:serviceType>
      <gco:LocalName codeSpace="www.w3c.org">
        <xsl:choose>
          <xsl:when test="$getCapabilities//*:ExtendedCapabilities/inspire_common:SpatialDataServiceType">
            <xsl:value-of select="$getCapabilities//*:ExtendedCapabilities/inspire_common:SpatialDataServiceType"/>
          </xsl:when>
          <xsl:when test="$rootName = ('WMT_MS_Capabilities', 'WMS_Capabilities')">OGC:WMS</xsl:when>
          <xsl:when test="$rootName = ('WFS_MS_Capabilities', 'WFS_Capabilities')">OGC:WFS</xsl:when>
          <xsl:when test="$rootName = ('WCS_Capabilities')">OGC:WCS</xsl:when>
          <xsl:when test="$rootName = ('Capabilities')">OGC:WPS</xsl:when>
          <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
        </xsl:choose>
      </gco:LocalName>
    </srv:serviceType>

    <srv:serviceTypeVersion>
      <gco:CharacterString>
        <xsl:value-of select='$getCapabilities/*/@version'/>
      </gco:CharacterString>
    </srv:serviceTypeVersion>
  </xsl:template>


  <xsl:template mode="copy-or-inject"
                match="srv:couplingType">

    <srv:couplingType>
      <srv:SV_CouplingType codeList="./resources/codeList.xml#SV_CouplingType">
        <xsl:attribute name="codeListValue">
          <xsl:choose>
            <xsl:when test="name(.)='wps:Capabilities' or
                            name(.)='wps1:Capabilities'">loosely</xsl:when>
            <xsl:otherwise>tight</xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
      </srv:SV_CouplingType>
    </srv:couplingType>
  </xsl:template>


  <xsl:template mode="copy-or-inject"
                match="srv:containsOperations">

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
             Operation could be OGC standard operation described in specification
             OR a specific process in a WPS. In that case, each process are described
             as one operation.
         -->

    <xsl:for-each select="$getCapabilities/(
                                Capability/Request/*|
                                wfs:Capability/wfs:Request/*|
                                wms:Capability/wms:Request/*|
                                wcs:Capability/wcs:Request/*|
                                ows:OperationsMetadata/ows:Operation|
                                ows11:OperationsMetadata/ows:Operation|
                                wps:ProcessOfferings/*|
                                wps1:ProcessOfferings/*)">
      <!-- Some services provide information about ows:ExtendedCapabilities TODO ? -->
      <srv:containsOperations>
        <srv:SV_OperationMetadata>
          <srv:operationName>
            <gco:CharacterString>
              <xsl:choose>
                <xsl:when test="name(.)='wps:Process'">WPS Process:
                  <xsl:value-of select="ows:Title|ows11:Title"/>
                </xsl:when>
                <xsl:when test="$ows='true'">
                  <xsl:value-of select="@name"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="name(.)"/>
                </xsl:otherwise>
              </xsl:choose>
            </gco:CharacterString>
          </srv:operationName>
          <!--  CHECKME : DCPType/SOAP ? -->
          <xsl:for-each select="DCPType/HTTP/*|wfs:DCPType/wfs:HTTP/*|wms:DCPType/wms:HTTP/*|
              wcs:DCPType/wcs:HTTP/*|ows:DCP/ows:HTTP/*|ows11:DCP/ows11:HTTP/*">
            <srv:DCP>
              <srv:DCPList codeList="./resources/codeList.xml#DCPList">
                <xsl:variable name="dcp">
                  <xsl:choose>
                    <xsl:when
                      test="name(.)='Get' or name(.)='wfs:Get' or name(.)='wms:Get' or name(.)='wcs:Get' or name(.)='ows:Get' or name(.)='ows11:Get'">
                      HTTP-GET
                    </xsl:when>
                    <xsl:when
                      test="name(.)='Post' or name(.)='wfs:Post' or name(.)='wms:Post' or name(.)='wcs:Post' or name(.)='ows:Post' or name(.)='ows11:Post'">
                      HTTP-POST
                    </xsl:when>
                    <xsl:otherwise>WebServices</xsl:otherwise>
                  </xsl:choose>
                </xsl:variable>
                <xsl:attribute name="codeListValue">
                  <xsl:value-of select="$dcp"/>
                </xsl:attribute>
              </srv:DCPList>
            </srv:DCP>
          </xsl:for-each>

          <xsl:if test="name(.)='wps:Process' or name(.)='wps11:ProcessOfferings'">
            <srv:operationDescription>
              <gco:CharacterString>
                <xsl:value-of select="ows:Abstract|ows11:Title"/>
              </gco:CharacterString>
            </srv:operationDescription>
            <srv:invocationName>
              <gco:CharacterString>
                <xsl:value-of select="ows:Identifier|ows11:Identifier"/>
              </gco:CharacterString>
            </srv:invocationName>
          </xsl:if>

          <xsl:for-each
            select="Format|wms:Format|ows:Parameter[@name='AcceptFormats' or @name='outputFormat']">
            <srv:connectPoint>
              <gmd:CI_OnlineResource>
                <gmd:linkage>
                  <gmd:URL>
                    <xsl:choose>
                      <xsl:when test="$ows='true'">
                        <xsl:value-of
                          select="..//ows:Get[1]/@xlink:href"/><!-- FIXME supposed at least one Get -->
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:value-of select="..//*[1]/OnlineResource/@xlink:href|
                          ..//*[1]/wms:OnlineResource/@xlink:href"/>
                      </xsl:otherwise>
                    </xsl:choose>
                  </gmd:URL>
                </gmd:linkage>
                <gmd:protocol>
                  <gco:CharacterString>
                    <xsl:choose>
                      <xsl:when test="$ows='true'">
                        <xsl:value-of select="ows:Value"/>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:value-of select="."/>
                      </xsl:otherwise>
                    </xsl:choose>
                  </gco:CharacterString>
                </gmd:protocol>
                <gmd:description>
                  <gco:CharacterString>
                    Format :
                    <xsl:value-of select="."/>
                  </gco:CharacterString>
                </gmd:description>
                <gmd:function>
                  <CI_OnLineFunctionCode codeList="./resources/codeList.xml#CI_OnLineFunctionCode"
                                         codeListValue="information"/>
                </gmd:function>
              </gmd:CI_OnlineResource>
            </srv:connectPoint>
          </xsl:for-each>


          <!-- Some Operations in WFS 1.0.0 have no ResultFormat no CI_OnlineResource created
                            WCS has no output format
                    -->
          <xsl:for-each select="wfs:ResultFormat/*">
            <srv:connectPoint>
              <gmd:CI_OnlineResource>
                <gmd:linkage>
                  <gmd:URL>
                    <xsl:value-of select="../..//wfs:Get[1]/@onlineResource"/>
                  </gmd:URL>
                </gmd:linkage>
                <gmd:protocol>
                  <gco:CharacterString>
                    <xsl:value-of select="name(.)"/>
                  </gco:CharacterString>
                </gmd:protocol>
                <gmd:function>
                  <CI_OnLineFunctionCode codeList="./resources/codeList.xml#CI_OnLineFunctionCode"
                                         codeListValue="information"/>
                </gmd:function>
              </gmd:CI_OnlineResource>
            </srv:connectPoint>
          </xsl:for-each>
        </srv:SV_OperationMetadata>
      </srv:containsOperations>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        Done by harvester after data metadata creation
        <xsl:for-each select="//Layer[count(./*[name(.)='Layer'])=0] | FeatureType[count(./*[name(.)='FeatureType'])=0] | CoverageOfferingBrief[count(./*[name(.)='CoverageOfferingBrief'])=0]">
                <srv:operatesOn>
                        <MD_DataIdentification uuidref="">
                        <xsl:value-of select="Name"/>
                        </MD_DataIdentification>
                </srv:operatesOn>
        </xsl:for-each>
        -->
  </xsl:template>


  <!-- Do a copy of every nodes and attributes -->
  <xsl:template mode="copy"
                match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"
                           mode="copy-or-inject"/>
    </xsl:copy>
  </xsl:template>


  <!-- Remove geonet:* elements. -->
  <xsl:template mode="copy"
                match="geonet:*"
                priority="2"/>

</xsl:stylesheet>
