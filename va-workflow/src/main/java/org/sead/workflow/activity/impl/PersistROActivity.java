package org.sead.workflow.activity.impl;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;
import org.apache.commons.io.IOUtils;
import org.sead.workflow.activity.AbstractWorkflowActivity;
import org.sead.workflow.activity.SeadWorkflowActivity;
import org.sead.workflow.config.SeadWorkflowConfig;
import org.sead.workflow.context.SeadWorkflowContext;
import org.sead.workflow.util.Constants;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Responsible for persisting the RO in RO subsystem
 */
public class PersistROActivity extends AbstractWorkflowActivity {

    @Override
    public void execute(SeadWorkflowContext context, SeadWorkflowConfig config) {
        System.out.println("Executing activity : " + activityName);

        HashMap<String, String> activityParams = new HashMap<String, String>();
        for(SeadWorkflowActivity activity : config.getActivities()){
            AbstractWorkflowActivity abstractActivity = (AbstractWorkflowActivity)activity;
            if(abstractActivity.activityName.equals(activityName)){
                activityParams = abstractActivity.params;
                break;
            }
        }

        String ro = context.getProperty(Constants.JSON_RO);
        String roSystemUrl = activityParams.get("roSystemUrl");


        // Call RO Info System to persist the JSONLD RO
        WebResource webResource = Client.create().resource(
                roSystemUrl + "/resource/putjsonldro"
        );

        FormDataMultiPart form = new FormDataMultiPart();
        form.field("ro", ro);

        ClientResponse response = webResource
                .type(MediaType.MULTIPART_FORM_DATA)
                .post(ClientResponse.class, form);

        if(response.getStatus() == 200){
            StringWriter writer = new StringWriter();
            try {
                IOUtils.copy(response.getEntityInputStream(), writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            context.addProperty(Constants.RO_ID, writer.toString());
            System.out.println(PersistROActivity.class.getName() + " : Successfully registered in RO Info Subsystem");
        } else {
            System.out.println(PersistROActivity.class.getName() + " : Failed to register RO in RO Info Subsystem");
            // TODO handle exception
        }

    }

}
