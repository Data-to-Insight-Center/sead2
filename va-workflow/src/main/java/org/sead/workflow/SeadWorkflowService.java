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
import java.util.concurrent.Semaphore;

import org.sead.workflow.util.Constants;

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
     * @param roId - Research Object description
     * @param psId - ID of the Project Space that invoked this method
     * @return DOI that is assigned to the published RO
     */
    @GET
    @Path("/publishRO/{roId}")
    @Produces("application/json")
    public javax.ws.rs.core.Response publishRO(@PathParam("roId") String roId,
                                               @QueryParam("psId") String psId) throws InterruptedException {

        System.out.println("Main : Input JSON: " + roId);

        // A semaphore is used to pause the main thread and spawn another thread(WorkflowThread) to execute the activities
        // WorkflowThread executes activities and signal the main thread before calling MM
        // When main thread is resumed, it send a response to the caller of this method.
        // After signaling the main thread WorkflowThread calls the MM to publish the RO
        Semaphore semaphore = new Semaphore(0);

        SeadWorkflowContext context = new SeadWorkflowContext();
        WorkflowThread workflowThread = new WorkflowThread(semaphore, roId, psId, context);
        workflowThread.start(); // start the WorkflowThread thread

        System.out.println("Main : acquire semaphore..");
        semaphore.acquire(); // Pause the main thread
        System.out.println("Main: resume");

        // send response back to client when signalled by WorkflowThread
        return Response.ok(context.getProperty(Constants.JSON_RO)).build();
    }

}
