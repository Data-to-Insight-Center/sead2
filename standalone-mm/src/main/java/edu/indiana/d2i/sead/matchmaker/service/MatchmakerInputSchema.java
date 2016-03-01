
package edu.indiana.d2i.sead.matchmaker.service;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Map;


public class MatchmakerInputSchema {


    private String requestID;

    private String responseKey;

    private Request request;

    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The requestID
     */

    public String getRequestID() {
        return requestID;
    }

    /**
     * 
     * @param requestID
     *     The requestID
     */

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    /**
     * 
     * @return
     *     The responseKey
     */

    public String getResponseKey() {
        return responseKey;
    }

    /**
     * 
     * @param responseKey
     *     The responseKey
     */

    public void setResponseKey(String responseKey) {
        this.responseKey = responseKey;
    }

    /**
     * 
     * @return
     *     The request
     */

    public Request getRequest() {
        return request;
    }

    /**
     * 
     * @param request
     *     The request
     */

    public void setRequest(Request request) {
        this.request = request;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }


    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }


    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(requestID).append(responseKey).append(request).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof MatchmakerInputSchema) == false) {
            return false;
        }
        MatchmakerInputSchema rhs = ((MatchmakerInputSchema) other);
        return new EqualsBuilder().append(requestID, rhs.requestID).append(responseKey, rhs.responseKey).append(request, rhs.request).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
