/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.seadva.dataone;

import org.apache.commons.codec.binary.Hex;
import org.dataconservancy.dcs.index.api.BatchIndexer;
import org.dataconservancy.dcs.index.api.IndexServiceException;
import org.dataconservancy.dcs.index.dcpsolr.ROBatchIndexer;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsFixity;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.model.dcs.DcsResourceIdentifier;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadFile;
import org.seadva.model.SeadPerson;
import org.seadva.model.pack.ResearchObject;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * Monitor function
 */
//monitor/ping

@Path("/util")
public class DataOneUtil {

    @GET
    @Path("addObject")
    @Produces(MediaType.APPLICATION_JSON)
    public String addObject(@QueryParam("filePath") String filePath,
                         @QueryParam("id") String identifier,
                         @QueryParam("schema") String metaFormat) throws IndexServiceException {

        ResearchObject researchObject = new ResearchObject();

        String filename = ((String) filePath).split("/")[((String) filePath).split("/").length - 1].replace(".xml","");
        if(metaFormat == null)
            metaFormat = "http://www.fgdc.gov/schemas/metadata/fgdc-std-001-1998.xsd";
        if(identifier == null)
            identifier = filename;

        SeadFile metadataFile = new SeadFile();
        metadataFile.setId(filename);
        metadataFile.setName(filename);
        metadataFile.setSource("file://" + filePath);

        try {
            DigestInputStream digestStream =
                    new DigestInputStream(new FileInputStream(filePath), MessageDigest.getInstance("SHA-1"));
            if (digestStream.read() != -1) {
                byte[] buf = new byte[1024];
                while (digestStream.read(buf) != -1);
            }
            byte[] digest = digestStream.getMessageDigest().digest();
            DcsFixity fixity = new DcsFixity();
            fixity.setAlgorithm("SHA-1");
            fixity.setValue(new String(Hex.encodeHex(digest)));
            metadataFile.addFixity(fixity);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        DcsFormat metadataFormat = new DcsFormat();
        metadataFormat.setFormat(metaFormat);
        metadataFile.addFormat(metadataFormat);

        DcsResourceIdentifier dcsResourceIdentifier = new DcsResourceIdentifier();
        dcsResourceIdentifier.setIdValue(identifier);
        dcsResourceIdentifier.setTypeId("dataone");
        metadataFile.addAlternateId(dcsResourceIdentifier);

        File metaFile = new File(filePath);
        metadataFile.setSizeBytes(metaFile.length());

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        metadataFile.setMetadataUpdateDate(strDate);

        researchObject.addFile(metadataFile);

        BatchIndexer<ResearchObject> indexer = new ROBatchIndexer(SeadQueryService.solr, null);
        indexer.add(researchObject);
        indexer.close();

        return "{\n" +
                "  \"response\" : \"Successfully added object - " + identifier + "\"" +
                "}";
    }

    @GET
    @Path("removeObject")
    @Produces(MediaType.APPLICATION_JSON)
    public String removeObject(@QueryParam("id") String identifier) throws IndexServiceException {

        BatchIndexer<ResearchObject> indexer = new ROBatchIndexer(SeadQueryService.solr, null);
        indexer.remove(identifier);
        indexer.close();

        return "{\n" +
                "  \"response\" : \"Successfully removed object - " + identifier + "\"" +
                "}";
    }
}
