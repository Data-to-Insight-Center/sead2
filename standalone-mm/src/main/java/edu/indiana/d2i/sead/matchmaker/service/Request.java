
package edu.indiana.d2i.sead.matchmaker.service;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Map;


public class Request {


    private String operation;

    private String message;

    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The operation
     */

    public String getOperation() {
        return operation;
    }

    /**
     * 
     * @param operation
     *     The operation
     */

    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * 
     * @return
     *     The message
     */

    public String getMessage() {
        return message;
    }

    /**
     * 
     * @param message
     *     The message
     */

    public void setMessage(String message) {
        this.message = message;
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
        return new HashCodeBuilder().append(operation).append(message).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Request) == false) {
            return false;
        }
        Request rhs = ((Request) other);
        return new EqualsBuilder().append(operation, rhs.operation).append(message, rhs.message).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
