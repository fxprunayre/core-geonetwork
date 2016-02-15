<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/1.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
                xmlns:mas="http://standards.iso.org/iso/19115/-3/mas/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mda="http://standards.iso.org/iso/19115/-3/mda/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/1.0"
                xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/1.0"
                xmlns:mdt="http://standards.iso.org/iso/19115/-3/mdt/1.0"
                xmlns:mex="http://standards.iso.org/iso/19115/-3/mex/1.0"
                xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
                xmlns:mpc="http://standards.iso.org/iso/19115/-3/mpc/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/1.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/1.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:mac="http://standards.iso.org/iso/19115/-3/mac/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:ns3="http://www.w3.org/2001/SMIL20/"
                xmlns:ns9="http://www.w3.org/2001/SMIL20/Language"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:ogc="http://www.opengis.net/ogc"
                xmlns:ows="http://www.opengis.net/ows"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:solr="java:org.fao.geonet.solr.SolrUtils"
                xmlns:saxon="http://saxon.sf.net/"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all"
                version="2.0">

  <xsl:import href="metadata-inspire-constant.xsl"/>

  <xsl:template match="mdb:MD_Metadata"
                mode="extract-uuid">
    <xsl:value-of select="mdb:metadataIdentifier/mcc:MD_Identifier/mcc:code/gco:CharacterString[. != '']"/>
  </xsl:template>

  <xsl:template match="mdb:MD_Metadata" mode="index">
    <!-- Main variables for the document -->
    <xsl:variable name="identifier" as="xs:string"
                  select="mdb:metadataIdentifier/mcc:MD_Identifier/mcc:code/gco:CharacterString[. != '']"/>



    <xsl:variable name="mainLanguage" as="xs:string?"
                  select="mdb:defaultLocale/lan:PT_Locale/
                            lan:language/lan:LanguageCode/
                              @codeListValue[normalize-space(.) != '']"/>

    <xsl:variable name="otherLanguages" as="attribute()*"
                  select="mdb:otherLocale/lan:PT_Locale/
                            lan:language/lan:LanguageCode/
                              @codeListValue[normalize-space(.) != '']"/>

    <!-- Record is dataset if no hierarchyLevel -->
    <xsl:variable name="isDataset" as="xs:boolean"
                  select="
                      count(mdb:metadataScope[mdb:MD_MetadataScope/
                              mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue='dataset']) > 0 or
                      count(mdb:metadataScopel) = 0"/>
    <xsl:variable name="isService" as="xs:boolean"
                  select="
                      count(mdb:metadataScope[mdb:MD_MetadataScope/
                              mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue='service']) > 0"/>

    <xsl:message>#<xsl:value-of select="count(preceding-sibling::mdb:MD_Metadata)"/>. <xsl:value-of select="$identifier"/></xsl:message>

    <!-- Create a first document representing the main record. -->
    <doc>
      <field name="docType">metadata</field>
      <field name="documentStandard">iso19115-3</field>

      <!-- Index the metadata document as XML -->
      <field name="document"><xsl:value-of select="saxon:serialize(., 'default-serialize-mode')"/></field>
      <field name="id"><xsl:value-of select="$identifier"/></field>
      <field name="metadataIdentifier"><xsl:value-of select="$identifier"/></field>


      <xsl:for-each select="mdb:metadataStandard/cit:CI_Citation/cit:title/gco:CharacterString">
        <field name="standardName"><xsl:value-of select="normalize-space(.)"/></field>
      </xsl:for-each>



      <!-- Indexing record information -->
      <!-- # Date -->
      <!-- TODO improve date formatting maybe using Joda parser
      Select first one because some records have 2 dates !
      eg. fr-784237539-bdref20100101-0105
      -->
      <xsl:for-each select="mdb:dateInfo/
                              cit:CI_Date[cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'revision']/
                                cit:date/*[text() != '' and position() = 1]">
        <field name="dateStamp">
          <xsl:value-of select="if (name() = 'gco:Date' and string-length(.) = 4)
                                then concat(., '-01-01T00:00:00Z')
                                else if (name() = 'gco:Date' and string-length(.) = 7)
                                then concat(., '-01T00:00:00Z')
                                else if (name() = 'gco:Date' or string-length(.) = 10)
                                then concat(., 'T00:00:00Z')
                                else (
                                  if (ends-with(., 'Z'))
                                  then .
                                  else concat(., 'Z')
                                )"/>
        </field>
      </xsl:for-each>


      <!-- # Languages -->
      <field name="mainLanguage"><xsl:value-of select="$mainLanguage"/></field>

      <xsl:for-each select="$otherLanguages">
        <field name="otherLanguage"><xsl:value-of select="."/></field>
      </xsl:for-each>


      <!-- # Resource type -->
      <xsl:choose>
        <xsl:when test="$isDataset">
          <field name="resourceType">dataset</field>
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="mdb:metadataScope/mdb:MD_MetadataScope/
                                  mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue[normalize-space(.) != '']">
            <field name="resourceType"><xsl:value-of select="."/></field>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>


      <!-- Indexing metadata contact -->
      <xsl:apply-templates mode="index-contact" select="mdb:contact">
        <xsl:with-param name="fieldSuffix" select="''"/>
      </xsl:apply-templates>

      <!-- Indexing all codelist

      Indexing method is:
      <gmd:accessConstraints>
        <gmd:MD_RestrictionCode codeListValue="otherRestrictions"
        is indexed as
        codelist_accessConstraints:otherRestrictions

        Exclude some useless codelist like
        Contact role, Date type.
      -->
      <xsl:for-each select=".//*[@codeListValue != '' and
                            name() != 'cit:CI_RoleCode' and
                            name() != 'cit:CI_DateTypeCode' and
                            name() != 'lan:LanguageCode'
                            ]">
        <field name="codelist_{local-name(..)}"><xsl:value-of select="@codeListValue"/></field>
      </xsl:for-each>


      <!-- Indexing resource information
      TODO: Should we support multiple identification in the same record
      eg. nl db60a314-5583-437d-a2ff-1e59cc57704e
      Also avoid error when records contains multiple MD_IdentificationInfo
      or SRV_ServiceIdentification or a mix
      eg. de 8bb5334f-558b-982b-7b12-86ea486540d7
      -->
      <xsl:for-each select="mdb:identificationInfo[1]/*[1]">
        <xsl:for-each select="cit:citation/cit:CI_Citation">
          <field name="resourceTitle">
            <xsl:value-of select="cit:title/gco:CharacterString/text()"/>
          </field>
          <field name="resourceAltTitle">
            <xsl:value-of select="cit:alternateTitle/gco:CharacterString/text()"/>
          </field>

          <xsl:for-each select="cit:date/cit:CI_Date[cit:date/*/text() != '']">
            <xsl:variable name="dateType"
                          select="cit:dateType/cit:CI_DateTypeCode/@codeListValue"
                          as="xs:string?"/>
            <xsl:variable name="date"
                          select="string(cit:date/gco:Date|cit:date/gco:DateTime)"/>
            <field name="{$dateType}DateForResource"><xsl:value-of select="$date"/></field>
            <field name="{$dateType}YearForResource"><xsl:value-of select="substring($date, 0, 5)"/></field>
            <field name="{$dateType}MonthForResource"><xsl:value-of select="substring($date, 0, 8)"/></field>
          </xsl:for-each>

          <xsl:for-each select="cit:presentationForm/cit:CI_PresentationFormCode/@codeListValue[. != '']">
            <field name="presentationForm"><xsl:value-of select="."/></field>
          </xsl:for-each>
        </xsl:for-each>

        <field name="resourceAbstract">
          <xsl:value-of select="substring(mri:abstract/gco:CharacterString, 0, $maxFieldLength)"/>
        </field>


        <!-- Indexing resource contact -->
        <xsl:apply-templates mode="index-contact"
                             select="mri:pointOfContact">
          <xsl:with-param name="fieldSuffix" select="'ForResource'"/>
        </xsl:apply-templates>


        <xsl:for-each select="mri:credit/*[. != '']">
          <field name="resourceCredit"><xsl:value-of select="."/></field>
        </xsl:for-each>


        <xsl:for-each select="mri:graphicOverview/mcc:MD_BrowseGraphic/
                                mcc:fileName/gco:CharacterString[. != '']">
          <field name="overviewUrl"><xsl:value-of select="."/></field>
        </xsl:for-each>

        <xsl:for-each select="mri:defaultLocale/lan:PT_Locale/lan:language/lan:LanguageCode/@codeListValue">
          <field name="resourceLanguage"><xsl:value-of select="."/></field>
        </xsl:for-each>



        <!-- TODO: create specific INSPIRE template or mode -->
        <!-- INSPIRE themes

        Select the first thesaurus title because some records
        may contains many even if invalid.

        Also get the first title at it may happen that a record
        have more than one.

        Select any thesaurus having the title containing "INSPIRE themes".
        Some records have "GEMET-INSPIRE themes" eg. sk:ee041534-b8f3-4683-b9dd-9544111a0712
        Some other "GEMET - INSPIRE themes"

        Take in account gmd:descriptiveKeywords or srv:keywords
        -->
        <xsl:for-each
                select="*/mri:MD_Keywords[contains(
                     mri:thesaurusName[1]/cit:CI_Citation/
                       cit:title[1]/gco:CharacterString/text(),
                       'INSPIRE themes')]/mri:keyword/gco:CharacterString">

          <xsl:variable name="inspireTheme" as="xs:string"
                        select="solr:analyzeField('inspireTheme_syn', text())"/>

          <field name="inspireTheme_syn"><xsl:value-of select="text()"/></field>
          <field name="inspireTheme"><xsl:value-of select="$inspireTheme"/></field>

          <!--
          WARNING: Here we only index the first keyword in order
          to properly compute one INSPIRE annex.
          -->
          <xsl:if test="position() = 1">
            <field name="inspireThemeFirst_syn"><xsl:value-of select="text()"/></field>
            <field name="inspireThemeFirst"><xsl:value-of select="$inspireTheme"/></field>
            <field name="inspireAnnexForFirstTheme">
              <xsl:value-of select="solr:analyzeField('inspireAnnex_syn', $inspireTheme)"/>
            </field>
          </xsl:if>
          <field name="inspireAnnex">
            <xsl:value-of select="solr:analyzeField('inspireAnnex_syn', $inspireTheme)"/>
          </field>
        </xsl:for-each>

        <field name="numberOfInspireTheme"><xsl:value-of
                select="count(*/mri:MD_Keywords[contains(
                     mri:thesaurusName[1]/cit:CI_Citation/
                       cit:title[1]/gco:CharacterString/text(),
                       'GEMET - INSPIRE themes')]
                  /mri:keyword)"/></field>


        <!-- Index all keywords -->
        <xsl:for-each
                select="*/mri:MD_Keywords/
                          mri:keyword/gco:CharacterString|
                        */mri:MD_Keywords/
                          mri:keyword/lan:PT_FreeText/lan:textGroup/
                            lan:LocalisedCharacterString">
          <field name="tag"><xsl:value-of select="text()"/></field>
        </xsl:for-each>

        <!-- Index keywords which are of type place -->
        <xsl:for-each
                select="*/mri:MD_Keywords/
                          mri:keyword[mri:type/mri:MD_KeywordTypeCode/@codeListValue = 'place']/
                            gco:CharacterString|
                        */mri:MD_Keywords/
                          mri:keyword[mri:type/mri:MD_KeywordTypeCode/@codeListValue = 'place']/
                            lan:PT_FreeText/lan:textGroup/lan:LocalisedCharacterString">
          <field name="geotag"><xsl:value-of select="text()"/></field>
        </xsl:for-each>


        <!-- Index all keywords having a specific thesaurus -->
        <xsl:for-each
                select="*/mri:MD_Keywords[mri:thesaurusName]/
                            mri:keyword">

          <xsl:variable name="thesaurusName"
                        select="../mri:thesaurusName[1]/cit:CI_Citation/
                                  cit:title[1]/gco:CharacterString"/>

          <xsl:variable name="thesaurusId"
                        select="normalize-space(../mri:thesaurusName/cit:CI_Citation/
                                  cit:identifier/mcc:MD_Identifier/
                                    mcc:code/*)"/>

          <xsl:variable name="key">
            <xsl:choose>
              <xsl:when test="$thesaurusId != ''">
                <xsl:value-of select="$thesaurusId"/>
              </xsl:when>
              <!-- Try to build a thesaurus key based on the name
              by removing space - to be improved. -->
              <xsl:when test="normalize-space($thesaurusName) != ''">
                <xsl:value-of select="replace($thesaurusName, ' ', '')"/>
              </xsl:when>
            </xsl:choose>
          </xsl:variable>

          <xsl:if test="normalize-space($key) != ''">
            <!-- Index keyword characterString including multilingual ones
             and element like gmx:Anchor including the href attribute
             which may contains keyword identifier. -->
            <xsl:for-each select="*[normalize-space() != '']|
                                  */@xlink:href[normalize-space() != '']|
                                  lan:PT_FreeText/lan:textGroup/lan:LocalisedCharacterString[normalize-space() != '']">
              <field name="thesaurus_{$key}"><xsl:value-of select="normalize-space(.)"/></field>
            </xsl:for-each>
          </xsl:if>
        </xsl:for-each>



        <xsl:for-each select="mri:topicCategory/mri:MD_TopicCategoryCode">
          <field name="topic"><xsl:value-of select="."/></field>
          <!-- TODO: Get translation ? -->
        </xsl:for-each>



        <xsl:for-each select="mri:spatialResolution/mri:MD_Resolution">
          <xsl:for-each select="mri:equivalentScale/mri:MD_RepresentativeFraction/mri:denominator/gco:Integer[. != '']">
            <field name="resolutionScaleDenominator"><xsl:value-of select="."/></field>
          </xsl:for-each>

          <xsl:for-each select="mri:distance/gco:Distance[. != '']">
            <field name="resolutionDistance"> <xsl:value-of select="concat(., @uom)"/></field>
          </xsl:for-each>
        </xsl:for-each>

        <xsl:for-each select="mri:spatialRepresentationType/mcc:MD_SpatialRepresentationTypeCode/@codeListValue[. != '']">
          <field name="spatialRepresentationType"><xsl:value-of select="."/></field>
        </xsl:for-each>



        <xsl:for-each select="mri:resourceConstraints">
          <xsl:for-each select="*/mco:accessConstraints/mco:MD_RestrictionCode/@codeListValue[. != '']">
            <field name="accessConstraints"><xsl:value-of select="."/></field>
          </xsl:for-each>
          <xsl:for-each select="*/mco:otherConstraints/gco:CharacterString[. != '']">
            <field name="otherConstraints"><xsl:value-of select="."/></field>
          </xsl:for-each>
          <xsl:for-each select="*/mco:classification/mco:MD_ClassificationCode/@codeListValue[. != '']">
            <field name="constraintClassification"><xsl:value-of select="."/></field>
          </xsl:for-each>
          <xsl:for-each select="*/mco:useLimitation/gco:CharacterString[. != '']">
            <field name="useLimitation"><xsl:value-of select="."/></field>
          </xsl:for-each>
        </xsl:for-each>




        <xsl:for-each select="*/gex:EX_Extent">

          <xsl:for-each select="gex:geographicElement/gex:EX_GeographicDescription/
                                  gex:geographicIdentifier/mcc:MD_Identifier/
                                    mcc:code/gco:CharacterString[normalize-space(.) != '']">
            <field name="geoTag"><xsl:value-of select="."/></field>
          </xsl:for-each>

          <!-- TODO: index bounding polygon -->
          <xsl:for-each select=".//gex:EX_GeographicBoundingBox[
                                ./gex:westBoundLongitude/gco:Decimal castable as xs:decimal and
                                ./gex:eastBoundLongitude/gco:Decimal castable as xs:decimal and
                                ./gex:northBoundLatitude/gco:Decimal castable as xs:decimal and
                                ./gex:southBoundLatitude/gco:Decimal castable as xs:decimal
                                ]">
            <xsl:variable name="format" select="'#0.000000'"></xsl:variable>

            <xsl:variable name="w" select="format-number(./gex:westBoundLongitude/gco:Decimal/text(), $format)"/>
            <xsl:variable name="e" select="format-number(./gex:eastBoundLongitude/gco:Decimal/text(), $format)"/>
            <xsl:variable name="n" select="format-number(./gex:northBoundLatitude/gco:Decimal/text(), $format)"/>
            <xsl:variable name="s" select="format-number(./gex:southBoundLatitude/gco:Decimal/text(), $format)"/>

            <!-- Example: ENVELOPE(-10, 20, 15, 10) which is minX, maxX, maxY, minY order
            http://wiki.apache.org/solr/SolrAdaptersForLuceneSpatial4
            https://cwiki.apache.org/confluence/display/solr/Spatial+Search

            bbox field type limited to one. TODO
            <xsl:if test="position() = 1">
              <field name="bbox">
                <xsl:text>ENVELOPE(</xsl:text>
                <xsl:value-of select="$w"/>
                <xsl:text>,</xsl:text>
                <xsl:value-of select="$e"/>
                <xsl:text>,</xsl:text>
                <xsl:value-of select="$n"/>
                <xsl:text>,</xsl:text>
                <xsl:value-of select="$s"/>
                <xsl:text>)</xsl:text>
              </field>
            </xsl:if>
            -->
            <xsl:choose>
              <xsl:when test="-180 &lt;= number($e) and number($e) &lt;= 180 and
                              -180 &lt;= number($w) and number($w) &lt;= 180 and
                              -90 &lt;= number($s) and number($s) &lt;= 90 and
                              -90 &lt;= number($n) and number($n) &lt;= 90">
                <xsl:choose>
                  <xsl:when test="$e = $w and $s = $n">
                    <field name="geom">
                      <xsl:text>POINT(</xsl:text>
                      <xsl:value-of select="concat($w, ' ', $s)"/>
                      <xsl:text>)</xsl:text>
                    </field>
                  </xsl:when>
                  <xsl:when test="($e = $w and $s != $n) or ($e != $w and $s = $n)">
                    <!-- Probably an invalid bbox indexing a point only -->
                    <field name="geom">
                      <xsl:text>POINT(</xsl:text>
                      <xsl:value-of select="concat($w, ' ', $s)"/>
                      <xsl:text>)</xsl:text>
                    </field>
                  </xsl:when>
                  <xsl:otherwise>
                    <field name="geom">
                      <xsl:text>POLYGON((</xsl:text>
                      <xsl:value-of select="concat($w, ' ', $s)"/>
                      <xsl:text>,</xsl:text>
                      <xsl:value-of select="concat($e, ' ', $s)"/>
                      <xsl:text>,</xsl:text>
                      <xsl:value-of select="concat($e, ' ', $n)"/>
                      <xsl:text>,</xsl:text>
                      <xsl:value-of select="concat($w, ' ', $n)"/>
                      <xsl:text>,</xsl:text>
                      <xsl:value-of select="concat($w, ' ', $s)"/>
                      <xsl:text>))</xsl:text>
                    </field>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:otherwise></xsl:otherwise>
            </xsl:choose>

            <!--<xsl:value-of select="($e + $w) div 2"/>,<xsl:value-of select="($n + $s) div 2"/></field>-->
          </xsl:for-each>
        </xsl:for-each>


        <!-- Service information -->
        <xsl:for-each select="srv:serviceType/gco:LocalName">
          <field name="serviceType"><xsl:value-of select="text()"/></field>
          <xsl:variable name="inspireServiceType" as="xs:string"
                        select="solr:analyzeField(
                        'inspireServiceType', text(),
                        'query',
                        'org.apache.lucene.analysis.miscellaneous.KeepWordFilter',
                        0)"/>
          <xsl:if test="$inspireServiceType != ''">
            <field name="inspireServiceType"><xsl:value-of select="lower-case($inspireServiceType)"/></field>
          </xsl:if>
          <xsl:if test="following-sibling::srv:serviceTypeVersion">
            <field name="serviceTypeAndVersion">
              <xsl:value-of select="concat(
                        text(),
                        $separator,
                        following-sibling::srv:serviceTypeVersion/gco:CharacterString/text())"/>
            </field>
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>







      <xsl:for-each select="mdb:referenceSystemInfo/*">
        <xsl:for-each select="mrs:referenceSystemIdentifier/mcc:RS_Identifier">
          <xsl:variable name="crs" select="mcc:code/gco:CharacterString"/>

          <xsl:if test="$crs != ''">
            <field name="coordinateSystem"><xsl:value-of select="$crs"/></field>
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>





      <!-- INSPIRE Conformity -->

      <!-- Conformity for data sets -->
      <xsl:choose>
        <xsl:when test="$isDataset">
          <xsl:for-each-group select="mdb:dataQualityInfo/*/mdq:report"
                              group-by="*/mdq:result/*/mdq:specification/cit:CI_Citation/
                                            cit:title/gco:CharacterString">

            <xsl:variable name="title" select="current-grouping-key()"/>
            <xsl:if test="count($eu10892010/*[lower-case(normalize-space(.)) =
                lower-case(normalize-space($title))]) = 1">

              <xsl:variable name="pass" select="*/mdq:result/*/mdq:pass/gco:Boolean"/>
              <field name="inspireConformResource"><xsl:value-of select="$pass"/></field>
            </xsl:if>
          </xsl:for-each-group>
        </xsl:when>
        <xsl:otherwise>
          <!-- Conformity for services -->
          <xsl:for-each-group select="mdb:dataQualityInfo/*/mdq:report"
                              group-by="*/mdq:result/*/mdq:specification/cit:CI_Citation/
                                            cit:title/gco:CharacterString">

            <xsl:variable name="title" select="current-grouping-key()"/>
            <xsl:if test="count($eu9762009/*[lower-case(normalize-space(.)) =
                lower-case(normalize-space($title))]) = 1 or
                count($eu10892010/*[lower-case(normalize-space(.)) =
                lower-case(normalize-space($title))]) = 1">

              <xsl:variable name="pass" select="*/mdq:result/*/mdq:pass/gco:Boolean"/>
              <field name="inspireConformResource"><xsl:value-of select="$pass"/></field>
            </xsl:if>
          </xsl:for-each-group>
        </xsl:otherwise>
      </xsl:choose>



      <xsl:for-each select="mdb:resourceLineage/*">
        <xsl:for-each select="mrl:lineage/mrl:LI_Lineage/
                                mrl:statement/gco:CharacterString[. != '']">
          <field name="lineage"><xsl:value-of select="."/></field>
        </xsl:for-each>
      </xsl:for-each>


      <xsl:for-each select="mdb:dataQualityInfo/*">
        <!-- Indexing measure value -->
        <xsl:for-each select="mdq:report/*[
                normalize-space(mdq:nameOfMeasure/gco:CharacterString) != '']">
          <xsl:variable name="measureName"
                        select="normalize-space(mdq:nameOfMeasure/gco:CharacterString)"/>
          <xsl:for-each select="mdq:result/mdq:DQ_QuantitativeResult/mdq:value">
            <xsl:if test=". != ''">
              <field name="measure_{$measureName}"><xsl:value-of select="."/></field>
            </xsl:if>
          </xsl:for-each>
        </xsl:for-each>
      </xsl:for-each>


      <xsl:for-each select="mdb:distributionInfo/*">
        <xsl:for-each select="mrd:distributionFormat/*/mrd:formatSpecificationCitation/
                                cit:CI_Citation/cit:title/gco:CharacterString">
          <field name="format"><xsl:value-of select="."/></field>
        </xsl:for-each>

        <xsl:for-each select="mrd:transferOptions/*/
                                mrd:onLine/*[cit:linkage/gco:CharacterString != '']">
          <field name="linkUrl"><xsl:value-of select="cit:linkage/gco:CharacterString"/></field>
          <field name="linkProtocol"><xsl:value-of select="cit:protocol/gco:CharacterString/text()"/></field>
          <field name="link">
            <xsl:value-of select="cit:protocol/*/text()"/>
            <xsl:text>|</xsl:text>
            <xsl:value-of select="cit:linkage/*/text()"/>
            <xsl:text>|</xsl:text>
            <xsl:value-of select="cit:name/*/text()"/>
            <xsl:text>|</xsl:text>
            <xsl:value-of select="cit:description/*/text()"/>
          </field>
        </xsl:for-each>
      </xsl:for-each>

      <!-- Service/dataset relation. Create document for the association.
      Note: not used for indicators anymore
       This could be used to retrieve :
      {!child of=documentType:metadata}+documentType:metadata +id:9940c446-6fd4-4ab3-a4de-7d0ee028a8d1
      {!child of=documentType:metadata}+documentType:metadata +resourceType:service +serviceType:view
      {!child of=documentType:metadata}+documentType:metadata +resourceType:service +serviceType:download
       -->
      <xsl:for-each select="mdb:identificationInfo/srv:SV_ServiceIdentification/srv:operatesOn">
        <xsl:variable name="associationType" select="'operatesOn'"/>
        <xsl:variable name="serviceType" select="../srv:serviceType/gco:LocalName"/>
        <!--<xsl:variable name="relatedTo" select="@uuidref"/>-->
        <xsl:variable name="getRecordByIdId">
          <xsl:if test="@xlink:href != ''">
            <xsl:analyze-string select="@xlink:href"
                                regex=".*[i|I][d|D]=([\w\-\.\{{\}}]*).*">
              <xsl:matching-substring>
                <xsl:value-of select="regex-group(1)"/>
              </xsl:matching-substring>
            </xsl:analyze-string>
          </xsl:if>
        </xsl:variable>

        <xsl:variable name="datasetId">
          <xsl:choose>
            <xsl:when test="$getRecordByIdId != ''">
              <xsl:value-of select="$getRecordByIdId"/>
            </xsl:when>
            <xsl:when test="@uuidref != ''">
              <xsl:value-of select="@uuidref"/>
            </xsl:when>
          </xsl:choose>
        </xsl:variable>

        <xsl:if test="$datasetId != ''">
          <field name="recordOperateOn"><xsl:value-of select="$datasetId"/></field>

          <!--<doc>
            <field name="id"><xsl:value-of
                    select="concat('association', $identifier,
              $associationType, $datasetId)"/></field>
            <field name="documentType">association</field>
            <field name="record"><xsl:value-of select="$identifier"/></field>
            <field name="associationType"><xsl:value-of select="$associationType"/></field>
            <field name="relatedTo"><xsl:value-of select="$datasetId"/></field>
          </doc>
          <doc>
            <field name="id"><xsl:value-of
                    select="concat('association',
              $associationType, $datasetId)"/></field>
            <field name="documentType">association2</field>
            <field name="record"><xsl:value-of select="$identifier"/></field>
            <field name="associationType"><xsl:value-of select="concat($associationType, $serviceType)"/></field>
            <field name="relatedTo"><xsl:value-of select="$datasetId"/></field>
          </doc>-->
        </xsl:if>
      </xsl:for-each>

      <!-- Index more fields in this element -->
      <xsl:apply-templates mode="index-extra-fields" select="."/>
    </doc>

    <!-- Index more documents for this element -->
    <xsl:apply-templates mode="index-extra-documents" select="."/>

    <!-- Create or update child document and register service relation
    in recordOperatedBy field and als associated resources.

     TODO: Some countries are using uuidref to store
     resource identifier and not metadata identifier. -->
    <xsl:for-each select="mdb:identificationInfo/srv:SV_ServiceIdentification/srv:operatesOn">
      <!--<xsl:message>## child record <xsl:value-of select="@xlink:href"/> </xsl:message>-->

      <!--
      uuiref store resource identifier and not metadata identifier.
          <xsl:variable name="relatedTo" select="@uuidref"/>
      -->
      <xsl:variable name="relatedTo">
        <xsl:if test="@xlink:href != ''">
          <xsl:analyze-string select="@xlink:href"
                              regex=".*id=([\w-]*).*">
            <xsl:matching-substring>
              <xsl:value-of select="regex-group(1)"/>
            </xsl:matching-substring>
          </xsl:analyze-string>
        </xsl:if>
      </xsl:variable>
      <!--<xsl:message>## child record <xsl:value-of select="$relatedTo"/> </xsl:message>-->

      <xsl:choose>
        <xsl:when test="$relatedTo">
          <doc>
            <field name="id"><xsl:value-of select="$relatedTo"/></field>
            <field name="metadataIdentifier" update="set"><xsl:value-of select="$relatedTo"/></field>
            <field name="recordOperatedBy" update="add"><xsl:value-of select="$identifier"/></field>
            <xsl:for-each select="../srv:serviceType/gco:LocalName">
              <field name="recordOperatedByType" update="add"><xsl:value-of select="."/></field>
            </xsl:for-each>
          </doc>
        </xsl:when>
        <xsl:otherwise>
          <xsl:message>Failed to extract metadata identifier from @uuidref or xlink:href <xsl:value-of select="@xlink:href"/></xsl:message>
        </xsl:otherwise>
      </xsl:choose>

    </xsl:for-each>
  </xsl:template>


  <xsl:template mode="index-contact" match="*[cit:CI_Responsibility]">
    <xsl:param name="fieldSuffix" select="''" as="xs:string"/>

    <!-- Select the first child which should be a CI_ResponsibleParty.
    Some records contains more than one CI_ResponsibleParty which is
    not valid and they will be ignored.
     Same for organisationName eg. de:b86a8604-bf78-480f-a5a8-8edff5586679 -->
    <xsl:variable name="organisationName"
                  select="*[1]/cit:party/cit:CI_Organisation/cit:name/gco:CharacterString"
                  as="xs:string*"/>

    <xsl:variable name="role"
                  select="*[1]/cit:role/*/@codeListValue"
                  as="xs:string?"/>
    <xsl:if test="normalize-space($organisationName) != ''">
      <field name="Org{$fieldSuffix}"><xsl:value-of select="$organisationName"/></field>
      <field name="{$role}Org{$fieldSuffix}"><xsl:value-of select="$organisationName"/></field>
    </xsl:if>
    <field name="contact{$fieldSuffix}">{
      org:"<xsl:value-of select="replace($organisationName, '&quot;', '\\&quot;')"/>",
      role:"<xsl:value-of select="$role"/>"
      }
    </field>
  </xsl:template>
</xsl:stylesheet>