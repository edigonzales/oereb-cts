<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ili="http://www.interlis.ch/INTERLIS2.3" version="3.0">
    <xsl:output encoding="UTF-8" indent="yes" method="html"/>
    <xsl:template match="ili:SO_AGI_OEREB_CTS_20230819.Results">
        <html>
        <head>
            <title>ÖREB Compliance Test Suite</title>
            <meta name="description" content="ÖREB Compliance Test Suite"/>
            <meta name="keywords" content="ÖREB, Compliance, Test, Suite, CTS, Solothurn, AGI, SOGIS"/>
            <meta name="author" content="Stefan Ziegler" />
            <meta name="viewport" content="width=device-width, initial-scale=1" />
            <meta http-equiv="cache-control" content="no-cache"/>

            <style>
                html {
                    font-size: 16px;
                    color: #333333;
                }

                body {
                    margin: 0px;
                    padding: 0px;
                    background: #FFFFFF; 
                }

                body, div {
                    /*font-family: Frutiger, Arial, sans-serif;*/
                    font-family: sans-serif;
                }

                #container {
                    margin-left: auto;
                    margin-right: auto;
                    width: 1200px;
                    max-width: 95%;
                    background-color: #FFFFFF;        
                }

                .logo {
                    margin-top: 20px;
                    text-align: right;
                    min-width: 50px;
                }

                #logo {
                    max-width: 100%;
                    min-width: 200px;     
                }

                #title {
                    margin-top: 20px;
                    margin-bottom: 30px;
                    font-size: 28px;
                    font-weight: 700;
                }

                .header_left {
                    float: left;
                    width: 200px;
                    display: block;
                    line-height: 3;
                    background-color: #eee;
                }

                .header_right {
                    display: block;
                    line-height: 3;
                    background-color: #eee;
                }

                .line {
                    width:100%;
                    margin-top: 5px;
                    margin-bottom: 5px;
                    border-bottom: 1px solid Gainsboro;
                }

                h2 {
                    font-size: 18px;
                }

                h3 {
                    font-size: 16px;
                    font-style: italic;
                    font-weight: 700;
                    margin-top: 10px;
                    margin-bottom: 10px;
                }

                p {
                    font-size: 16px;
                    line-height: 1.5;
                    margin-bottom: 40px;
                }

                p.datenebene {
                    margin-top: 10px;
                    margin-bottom: 15px;
                }

                a.black-link {
                    overflow: hidden;
                    text-overflow: ellipsis;
                    color: #333333; 
                    text-decoration: none !important;
                }

                a.black-link:hover {
                    color: #333333;
                    text-decoration: underline !important;
                }  

                a.black-link:visited {
                    color: #333333; 
                    text-decoration: underline !important;
                }  

                a {
                    overflow: hidden;
                    text-overflow: ellipsis;
                    color: #c62828; 
                    text-decoration: none !important;
                }

                a:hover {
                    color: #c62828;
                    text-decoration: underline !important;
                }  

                a:visited {
                    color: #c62828; 
                    text-decoration: underline !important;
                }  

/*
                table { 
                    border-collapse: collapse; 
                    width: 100%;
                    table-layout: auto;
                    margin-bottom: 40px;
                }

                th {
                    text-align: left;
                    font-style: italic;
                    font-weight: 400;
                }

                tr {
                    line-height: 1.5;
                }

                tr:first-child {
                border-bottom: 1px solid #eee;;
                }

                tr:nth-child(even) {
                    background-color: rgba(237, 237, 237, 0.4);;
                }

                td {
                    vertical-align: top;
                }
*/
                @media print {

                    @page {
                        size: A4;
                    }

                    #logo {
                        width: 60mm;
                    }

                    #title { 
                        /*font-family: serif;*/
                        margin-top: 5mm;
                        font-size: 20pt; 
                    }

                    h2 {
                        font-size: 14pt;
                    }
                    
                    h3 {
                        font-size: 12pt;
                    }

                    p { 
                        font-size: 12pt;
                        line-height: 1.1;
                        margin-bottom: 8mm;
                    }

                    table { 
                        font-size: 10pt;
                    }

                }
            </style>
        </head>

        <body>
            <div id="container">
                <div class="logo">
                    <div>
                        <img id="logo" width="300" src="data:image/png;base64,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" alt="Logo PLR"/>
                    </div>
                </div>

<!--
                <div id="breadcrumb">
                    <i style="line-height:40px; vertical-align:middle;" ></i>
                    <ul class="breadcrumb">
                        <li><a href="https://geo.so.ch">Home</a></li>
                        <li><a href="https://sogis-sodata-kfjfk.ondigitalocean.app/">Datenbezug</a></li>
                        <li class="active">Datenbeschreibung</li>
                    </ul>
                </div>
-->
                <div id="title">
                    <!--<xsl:value-of select="title"/>-->
                    ÖREB Compliance Test Suite: Results
                </div>

                <xsl:for-each-group select="ili:SO_AGI_OEREB_CTS_20230819.Results.ProbeResult" group-by="ili:serviceEndpoint">
                    <div class="header_left" style="background-color: white; font-weight: 700">
                        Service endpoint:
                    </div>
                    <div class="header_right" style="background-color: white; font-weight: 700;">
                        <xsl:value-of select="ili:serviceEndpoint"/>
                    </div>

                    <xsl:for-each-group select="current-group()" group-by="ili:className">
                        <div class="header_left">
                            Probe:
                        </div>
                        <div class="header_right">
                            <xsl:value-of select="ili:className"/>
                        </div>
                        <xsl:for-each select="current-group()">
                            <div style="margin:-1px 0 0 -1px;">
                                <details style="line-height: 2.0;">
                                    <xsl:choose>
                                        <xsl:when test="ili:success='true'">
                                            <summary style="background-color: #92C5DE; border: 1px solid white; "><xsl:value-of select="ili:request"/></summary>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <summary style="background-color: #FAA582; border: 1px solid white;"><xsl:value-of select="ili:request"/></summary>
                                        </xsl:otherwise>
                                    </xsl:choose>

                                    <table width="100%;" style="line-height: 1.5;">
                                        <colgroup>
                                        <col span="1" style="width: 15%;"/>
                                        <col span="1" style="width: 85%;"/>
                                        </colgroup>
                                        <tr>
                                            <td>
                                                Request:
                                            </td>
                                            <td>
                                                <xsl:element name="a">
                                                    <xsl:attribute name="target">
                                                        <xsl:text>_blank</xsl:text>
                                                    </xsl:attribute>
                                                    <xsl:attribute name="class">
                                                        <xsl:text>black-link</xsl:text>
                                                    </xsl:attribute>
                                                    <xsl:attribute name="href"><xsl:value-of select="ili:request"/></xsl:attribute>
                                                    <xsl:value-of select="ili:request"/>
                                                </xsl:element>

                                            </td>
                                        </tr>

                                        <tr>
                                            <td>
                                                Request time (s):
                                            </td>
                                            <td>
                                                <xsl:value-of select="ili:processingTimeSecs"/>
                                            </td>
                                        </tr>
                                    </table>

                                    <div class="line"></div>

                                    <xsl:for-each select="ili:checkResults/ili:SO_AGI_OEREB_CTS_20230819.CheckResult">
                                        <table width="100%;" style="line-height: 1.5;">
                                            <colgroup>
                                            <col span="1" style="width: 15%;"/>
                                            <col span="1" style="width: 85%;"/>
                                            </colgroup>

                                            <tr>
                                                <td>
                                                    Check:
                                                </td>
                                                <td>
                                                    <xsl:value-of select="ili:className"/>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td>
                                                    Description:
                                                </td>
                                                <td>
                                                    <xsl:value-of select="ili:description"/>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td>
                                                    Success:
                                                </td>
                                                <td>
                                                    <xsl:choose>
                                                        <xsl:when test="ili:success='true'">
                                                            <xsl:value-of select="ili:success"/>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <span style="color: #FAA582; font-weight: 700;">
                                                                <xsl:value-of select="ili:success"/>
                                                            </span>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                </td>
                                            </tr>
                                            <xsl:if test="ili:statusCode">
                                                <tr>
                                                    <td>
                                                        Status code:
                                                    </td>
                                                    <td>
                                                        <xsl:value-of select="ili:statusCode"/>
                                                    </td>
                                                </tr>
                                            </xsl:if>
                                            <xsl:if test="message">
                                                <tr>
                                                    <td>
                                                        Message:
                                                    </td>
                                                    <td>
                                                        <xsl:value-of select="ili:message"/>
                                                    </td>
                                                </tr>
                                            </xsl:if>


                                        </table>

                                        <div class="line"></div>
                                    </xsl:for-each>                 




                                </details>
                            </div>
                        </xsl:for-each>

                        <div style="line-height: 0.5">
                            &#160;
                        </div>

                    </xsl:for-each-group>
                    <div style="line-height: 2">
                        &#160;
                    </div>

                    <!-- <xsl:message>Hallo</xsl:message> -->
                </xsl:for-each-group>
            </div>
        </body>
        </html>
    </xsl:template>

</xsl:stylesheet>