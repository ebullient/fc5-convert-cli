<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:csv="csv:csv">
    <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>

    <xsl:variable name="delimiter" select="','"/>

    <csv:columns>
        <column>name</column>
        <column>type</column>
        <column>weight</column>
        <column>value</column>
        <column>magic</column>
        <column>ac</column>
        <column>strength</column>
        <column>stealth</column>
        <column>dmg1</column>
        <column>dmg2</column>
        <column>dmgType</column>
        <column>property</column>
        <column>detail</column>
        <column>text</column>
    </csv:columns>

    <xsl:template match="compendium">
        <!-- Output the CSV header -->
        <xsl:for-each select="document('')/*/csv:columns/*">
                <xsl:value-of select="."/>
                <xsl:if test="position() != last()">
                    <xsl:value-of select="$delimiter"/>
                </xsl:if>
        </xsl:for-each>
        <xsl:text>&#xa;</xsl:text>

        <!-- Output rows for each matched item -->
        <xsl:apply-templates select="item"/>
    </xsl:template>

    <xsl:template match="item">
        <xsl:variable name="item" select="."/>
        <!-- Loop through the columns in order  -->
        <xsl:for-each select="document('')/*/csv:columns/*">
            <!-- Extract the column name and value  -->
            <xsl:variable name="column" select="."/>
            <xsl:variable name="value" select="$item/*[name() = $column]"/>

            <!-- Quote the value if required -->
            <xsl:choose>
                <xsl:when test="contains($value, '&quot;')">
                    <xsl:variable name="x" select="replace($value, '&quot;',  '&quot;&quot;')"/>
                    <xsl:value-of select="concat('&quot;', $x, '&quot;')"/>
                </xsl:when>
                <xsl:when test="contains($value, $delimiter)">
                    <xsl:value-of select="concat('&quot;', $value, '&quot;')"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$value"/>
                </xsl:otherwise>
            </xsl:choose>

            <!-- Add the delimiter unless we are the last expression -->
            <xsl:if test="position() != last()">
                <xsl:value-of select="$delimiter"/>
            </xsl:if>
        </xsl:for-each>

        <!-- Add a newline at the end of the record -->
        <xsl:text>&#xa;</xsl:text>
    </xsl:template>
</xsl:stylesheet>
