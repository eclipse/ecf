<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:bm="http://www.eclipse.org/buckminster/RMap-1.0">
    <xsl:output method="xml" indent="yes"/>
    
    <xsl:strip-space elements="*"/>
    
    <!-- overwrite via parameters -->
    <xsl:param name="repository">http://download.eclipse.org/rt/ecf/3.4/site.p2/</xsl:param>
    <xsl:param name="title">Eclipse Communication Framework (ECF)</xsl:param>
    <xsl:param name="releaseType">R</xsl:param>
    <xsl:param name="contactName">ECF mailinglist</xsl:param>
    <xsl:param name="contactMail">ecf-dev@eclipse.org</xsl:param>
    
    <xsl:template match="/">
        <aggregator:Aggregator 
            xmi:version="2.0" 
            xmlns:xmi="http://www.omg.org/XMI" 
            xmlns:aggregator="http://www.eclipse.org/b3/2010/aggregator/1.0.0" 
            buildmaster="//@contacts[email='{$contactMail}']" 
            label="{$title}"
            type="{$releaseType}" 
            mavenResult="true">
            
            <configurations architecture="x86_64"/>
            <configurations operatingSystem="linux" windowSystem="gtk"/>
            <configurations operatingSystem="linux" windowSystem="gtk" architecture="x86_64"/>
 
            <contributions label="{$title}">
                <repositories location="{$repository}"/>
            </contributions>

            <contacts name="{$contactName}" email="{$contactMail}"/>

            <xsl:apply-templates/>
            
        </aggregator:Aggregator>
    </xsl:template>
    
    <xsl:template match="bm:provider">
    	<!-- only accept public http|ftp repositories -->
        <xsl:if test="starts-with(bm:uri/@format, 'http://') or starts-with(bm:uri/@format, 'ftp://')">
        
        	<!-- remove dangling ?importType=binary -->
        	<xsl:choose>
        		<xsl:when test="contains(bm:uri/@format, '?importType=binary')">
		        	<validationRepositories location="{substring-before(bm:uri/@format, '?importType=binary')}"/>
        		</xsl:when>
        		<xsl:otherwise>
            		<validationRepositories location="{bm:uri/@format}"/>
        		</xsl:otherwise>
        	</xsl:choose>
        </xsl:if>
    </xsl:template>    
    
</xsl:stylesheet>
