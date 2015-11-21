<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >


    <xsl:template match="dateSysMetadataModified">
            <xsl:if test="contains(.,'+00:00')">
                <xsl:element name="dateSysMetadataModified">
                    <xsl:value-of select="concat(substring-before(.,'+00:00'),'Z')"/>
                </xsl:element>
            </xsl:if>
    </xsl:template>

    <xsl:template match="dateUploaded">
        <xsl:if test="contains(.,'+00:00')">
            <xsl:element name="dateUploaded">
                <xsl:value-of select="concat(substring-before(.,'+00:00'),'Z')"/>
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>


</xsl:stylesheet>