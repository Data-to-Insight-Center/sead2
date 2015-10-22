/*
 * Copyright 2015 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author isuriara@indiana.edu
 */

package org.sead.workflow;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.sead.workflow.activity.SeadWorkflowActivity;
import org.sead.workflow.config.SeadWorkflowConfig;
import org.sead.workflow.context.SeadWorkflowContext;
import org.sead.workflow.exception.SeadWorkflowException;
import org.sead.workflow.util.Constants;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.namespace.QName;
import java.io.InputStream;
import java.util.Iterator;

@Path("service")
public class SeadWorkflowService {

    // log init
    private static final Log log = LogFactory.getLog(SeadWorkflowService.class);

    // workflow config which is shared among all invocations
    public static SeadWorkflowConfig config = new SeadWorkflowConfig();

    static {
        try {
            // reads the sead-wf.xml to load the workflow configuration
            InputStream inputStream =
                    SeadWorkflowService.class.getResourceAsStream("sead-wf.xml");
            OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(inputStream);
            builder.setCache(true);
            OMElement docElement = builder.getDocumentElement();

            // read config level parameters first
            Iterator paramItr = docElement.getChildrenWithLocalName("parameter");
            while (paramItr.hasNext()) {
                OMElement param = (OMElement) paramItr.next();
                config.addParam(param.getAttributeValue(new QName("name")), param.getText());
            }

            // read activity list
            OMElement activities = docElement.getFirstChildWithName(new QName("activities"));
            Iterator activityItr = activities.getChildElements();
            // go through all activities and load them into SEAD config
            while (activityItr.hasNext()) {
                OMElement activity = (OMElement) activityItr.next();
                String className = activity.getAttributeValue(new QName("class"));
                // load activity class using Thread Context class loader
                Class c = Thread.currentThread().getContextClassLoader().loadClass(className);
                SeadWorkflowActivity wfActivity = (SeadWorkflowActivity) c.newInstance();
                wfActivity.setName(activity.getAttributeValue(new QName("name")));
                wfActivity.setTransactional(activity.getAttributeValue(new QName("transactional")).equalsIgnoreCase(Constants.TRUE) ? true : false);
                // read activity parameters
                paramItr = activity.getChildrenWithLocalName("parameter");
                while (paramItr.hasNext()) {
                    OMElement param = (OMElement) paramItr.next();
                    wfActivity.addParam(param.getAttributeValue(new QName("name")), param.getText());
                }
                config.addActivity(wfActivity);
            }
            log.info("Done loading SEAD Configuration");
        } catch (Exception e) {
            log.error("Error while loading SEAD Configuration.", e);
        }
    }

    /**
     * Ping method to check whether the workflow service is up
     *
     * @return ACK
     */
    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String ping() {
        return "SEAD Workflow Service is up!";
    }

    /**
     * Invokes the publish workflow to publish the given Research Object.
     *
     * @param ro - Research Object description
     * @return response to publishRO request
     */
    @POST
    @Path("/publishRO")
    @Produces("application/json")
    public javax.ws.rs.core.Response publishRO(String ro, @QueryParam("requestUrl") String requestURL) throws InterruptedException {

        System.out.println("-----------------------------------");
        System.out.println("SeadWorkflowService - publishRO : Input RO : " + ro);
        System.out.println("-----------------------------------");

        String id = null;

        try {
            JSONObject roObject = new JSONObject(ro);
            JSONObject aggregation = (JSONObject)roObject.get("Aggregation");
            id = aggregation.get("Identifier").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SeadWorkflowContext context = new SeadWorkflowContext();
        context.addProperty(Constants.JSON_RO, ro);
        context.addProperty(Constants.REQUEST_URL, requestURL);
        context.setCollectionId(id);

        String response = "";

        int executedActivities = 0;
        for (SeadWorkflowActivity activity : SeadWorkflowService.config.getActivities()) {
            // execute activities
            try {
                activity.execute(context, SeadWorkflowService.config);
                executedActivities++;
            } catch (SeadWorkflowException e) {
                System.out.println("*** WorkflowThread : exception... ***");
                e.printStackTrace();
                System.out.println("*** WorkflowThread : Breaking the MicroService loop ***");

                System.out.println("*** WorkflowThread : Rollback previous activities ***");
                for(int i = executedActivities - 1 ; i > -1 ; i--){
                    if(SeadWorkflowService.config.getActivities().get(i).getTransactional() == true) {
                        System.out.println("*** WorkflowThread : Rollback " + SeadWorkflowService.config.getActivities().get(i).getClass().getName());
                        SeadWorkflowService.config.getActivities().get(i).rollback(context, SeadWorkflowService.config);
                    }
                }

                response = "{\"response\": \"failure\", \"message\": \"" + e.getMessage() + "\"}";
                System.out.println("-----------------------------------");
                System.out.println("SeadWorkflowService - Respond to publishRO request : " + response);
                System.out.println("-----------------------------------");
                return Response.serverError().entity(response).type(MediaType.APPLICATION_JSON_TYPE).build();
            }
        }

        response = "{\"response\": \"success\", \"message\": \"Research Object was published successfully\"}";
        System.out.println("-----------------------------------");
        System.out.println("SeadWorkflowService - Respond to publishRO request : " + response);
        System.out.println("-----------------------------------");
        return Response
                .ok(response)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }

}
