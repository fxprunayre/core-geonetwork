<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gmi="http://www.isotc211.org/2005/gmi"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/1.0"
                xmlns:saxon="http://saxon.sf.net/"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all"
                version="2.0">

  <xsl:output name="default-serialize-mode"
              indent="no"
              omit-xml-declaration="yes"
              encoding="utf-8"
              escape-uri-attributes="yes"/>

  <xsl:param name="indexingIndex" select="'0'"/>

  <xsl:variable name="dateFormat" as="xs:string"
                select="'[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]Z'"/>

  <xsl:variable name="separator" as="xs:string"
                select="'|'"/>

  <!-- To avoid Document contains at least one immense term
  in field="resourceAbstract" (whose UTF8 encoding is longer
  than the max length 32766. -->
  <xsl:variable name="maxFieldLength" select="32000" as="xs:integer"/>

  <xsl:include href="metadata-iso19139.xsl"/>
  <xsl:include href="metadata-iso19115-3.xsl"/>


  <xsl:template match="*"
                mode="extract-uuid"/>

  <xsl:template match="/">
    <!-- Add a Solr document -->
    <add>
      <!-- For any ISO19139 records in the input XML document
      Some records from IS do not have record identifier. Ignore them.
      -->
      <xsl:variable name="records"
                    select="//(gmi:MI_Metadata|gmd:MD_Metadata|mdb:MD_Metadata)"/>

      <!-- Check number of records returned and reported -->
      <xsl:message>======================================================</xsl:message>


      <!-- Check duplicates
      TODO: Support ISO19115-3 check
      -->
      <xsl:for-each select="$records">
        <xsl:variable name="identifier" as="xs:string">
          <xsl:apply-templates mode="extract-uuid" select="."/>
        </xsl:variable>

        <xsl:variable name="numberOfRecordWithThatUUID"
                      select="count(../*[gmd:fileIdentifier/gco:CharacterString = $identifier])"/>
        <xsl:if test="$numberOfRecordWithThatUUID > 1">
          <xsl:message>WARNING: <xsl:value-of select="$numberOfRecordWithThatUUID"/> record(s) having UUID '<xsl:value-of select="$identifier"/>' in that set.</xsl:message>
        </xsl:if>
      </xsl:for-each>


      <xsl:for-each select="$records">

        <xsl:variable name="identifier" as="xs:string">
          <xsl:apply-templates mode="extract-uuid" select="."/>
        </xsl:variable>

        <xsl:choose>
          <xsl:when test="normalize-space($identifier) = ''">
            <xsl:message>WARNING: Record with null UUID found.</xsl:message>
            <xsl:message><xsl:copy-of select="."/></xsl:message>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates mode="index" select="."/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </add>
  </xsl:template>

  <!-- For each record, the main mode 'index' is called,
  then in the document node the mode 'index-extra-fields'
  could be used to index more fields. -->
  <xsl:template mode="index-extra-fields" match="*"/>

  <!-- then after the doc, the 'index-extra-documents' mode
  could be used to create more doc. -->
  <xsl:template mode="index-extra-documents" match="*"/>

</xsl:stylesheet>