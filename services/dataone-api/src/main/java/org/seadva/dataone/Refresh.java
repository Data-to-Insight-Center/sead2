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

import org.apache.solr.client.solrj.SolrServerException;
import org.xml.sax.SAXException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * To refresh loading from solr index to see if any new objects were added
 */

@Path("/refresh")
public class Refresh {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String doRefresh() throws IOException, SAXException, ParserConfigurationException, SolrServerException {
        SeadQueryService.solr.server().commit();
        return "Refreshed reading from Solr Index";
    }
}
