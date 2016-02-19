/*
#
# Copyright 2012 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: XSDview
# File:  SeadConstants.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package org.seadva.metadatagen.util;

import noNamespace.*;

/**
 * @author Yiming Sun
 * Modified by Kavitha Chandrasekar
 */
public class SeadNCEDConstants {
    
    public static final String DEFAULT_ORIGINATOR = "None";
    public static final String DEFAULT_PUBDATE = "20070101";
    public static final String DEFAULT_ABSTRACT = "All data created or compiled by Sustainability Science Research Scientists.";
    public static final String DEFAULT_PURPOSE = "Data related to Sustainability Science Research";
    public static final String DEFAULT_CONTACTPERSON = "None";
    
    public static final String DEFAULT_BEGINDATE = "20070101";
    public static final String DEFAULT_ENDDATE = "20070101";
    
    public static final String DEFAULT_UUID = "Default";
    public static final String DEFAULT_CURRENTREF = "ground condition";
    
    public static final ProgressType.Enum DEFAULT_PROGRESS = ProgressType.COMPLETE;
    public static final String DEFAULT_MAINTUPDATEFREQ = "As needed";

    public static final double DEFAULT_WESTBOUND = -180;
    public static final double DEFAULT_EASTBOUND = 180;
    public static final double DEFAULT_NORTHBOUND = 90;
    public static final double DEFAULT_SOUTHBOUND = -90;


    public static final String DEFAULT_THEMEKT = "None";
    public static final String[] DEFAULT_THEMEKEYS = {"None"};

    public static final String DEFAULT_PLACEKT = "None";
    public static final String[] DEFAULT_PLACEKEYS = {"None"};
    
    public static final String DEFAULT_TEMPORALKT = "None";
    public static final String[] DEFAULT_TEMPORALKEYS = {"None"};
    
    public static final String DEFAULT_ACCESSCONSTRAINT = "Public";
    public static final String DEFAULT_USECONSTRAINT = "Default";
    
    public static final String DEFAULT_METD = "20070101";
    
    public static final CntinfoType DEFAULT_METADATACONTACT = CntinfoType.Factory.newInstance();
    static {
        CntperpType cntperpType = DEFAULT_METADATACONTACT.addNewCntperp();
        cntperpType.setCntper(DEFAULT_CONTACTPERSON);
        CntaddrType cntaddrType = DEFAULT_METADATACONTACT.addNewCntaddr();
        cntaddrType.setAddrtype("None");
        cntaddrType.setCity("None");
        cntaddrType.setState("None");
        cntaddrType.setPostal("None");
        CntvoiceType cntvoiceType = DEFAULT_METADATACONTACT.addNewCntvoice();
        cntvoiceType.setStringValue("None");
    };
    
    public static final String DEFAULT_METADATANAME = "FGDC Standard for Digital Geospatial Metadata";
    public static final String DEFAULT_METADATAVERS = "FGDC-STD-001-1998";
}


