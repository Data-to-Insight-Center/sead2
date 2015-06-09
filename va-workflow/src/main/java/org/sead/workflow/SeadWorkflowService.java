package org.sead.workflow;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sead.workflow.activity.SeadWorkflowActivity;
import org.sead.workflow.config.SeadWorkflowConfig;
import org.sead.workflow.context.SeadWorkflowContext;

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
    private static SeadWorkflowConfig config = new SeadWorkflowConfig();

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
                // read activity parameters
                paramItr = docElement.getChildrenWithLocalName("parameter");
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
     * @return DOI that is assigned to the published RO
     */
    @POST
    @Path("/publishRO")
    @Consumes("application/json")
    public String publishRO(String ro) {
        System.out.println("Input JSON: " + ro);
        SeadWorkflowContext context = new SeadWorkflowContext();
        for (SeadWorkflowActivity activity : config.getActivities()) {
            activity.execute(context, config);
        }
//        return Response.ok().build();
        return "SEAD Publish Workflow triggered!";
    }

}
