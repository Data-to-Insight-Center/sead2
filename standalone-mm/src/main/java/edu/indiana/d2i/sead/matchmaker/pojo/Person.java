
package edu.indiana.d2i.sead.matchmaker.pojo;

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "@context",
    "@type",
    "name",
    "@id",
    "affiliation",
    "jobTitle",
    "email",
    "URL",
    "codeRepository"
})
public class Person {

    @JsonProperty("@context")
    private String Context;
    @JsonProperty("@type")
    private String Type;
    @JsonProperty("name")
    private String name;
    @JsonProperty("@id")
    private String Id;
    @JsonProperty("affiliation")
    private String affiliation;
    @JsonProperty("jobTitle")
    private String jobTitle;
    @JsonProperty("email")
    private String email;
    @JsonProperty("URL")
    private String URL;
    @JsonProperty("codeRepository")
    private String codeRepository;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The Context
     */
    @JsonProperty("@context")
    public String getContext() {
        return Context;
    }

    /**
     * 
     * @param Context
     *     The @context
     */
    @JsonProperty("@context")
    public void setContext(String Context) {
        this.Context = Context;
    }

    /**
     * 
     * @return
     *     The Type
     */
    @JsonProperty("@type")
    public String getType() {
        return Type;
    }

    /**
     * 
     * @param Type
     *     The @type
     */
    @JsonProperty("@type")
    public void setType(String Type) {
        this.Type = Type;
    }

    /**
     * 
     * @return
     *     The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * @return
     *     The Id
     */
    @JsonProperty("@id")
    public String getId() {
        return Id;
    }

    /**
     * 
     * @param Id
     *     The @id
     */
    @JsonProperty("@id")
    public void setId(String Id) {
        this.Id = Id;
    }

    /**
     * 
     * @return
     *     The affiliation
     */
    @JsonProperty("affiliation")
    public String getAffiliation() {
        return affiliation;
    }

    /**
     * 
     * @param affiliation
     *     The affiliation
     */
    @JsonProperty("affiliation")
    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    /**
     * 
     * @return
     *     The jobTitle
     */
    @JsonProperty("jobTitle")
    public String getJobTitle() {
        return jobTitle;
    }

    /**
     * 
     * @param jobTitle
     *     The jobTitle
     */
    @JsonProperty("jobTitle")
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    /**
     * 
     * @return
     *     The email
     */
    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    /**
     * 
     * @param email
     *     The email
     */
    @JsonProperty("email")
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 
     * @return
     *     The URL
     */
    @JsonProperty("URL")
    public String getURL() {
        return URL;
    }

    /**
     * 
     * @param URL
     *     The URL
     */
    @JsonProperty("URL")
    public void setURL(String URL) {
        this.URL = URL;
    }

    /**
     * 
     * @return
     *     The codeRepository
     */
    @JsonProperty("codeRepository")
    public String getCodeRepository() {
        return codeRepository;
    }

    /**
     * 
     * @param codeRepository
     *     The codeRepository
     */
    @JsonProperty("codeRepository")
    public void setCodeRepository(String codeRepository) {
        this.codeRepository = codeRepository;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(Context).append(Type).append(name).append(Id).append(affiliation).append(jobTitle).append(email).append(URL).append(codeRepository).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Person) == false) {
            return false;
        }
        Person rhs = ((Person) other);
        return new EqualsBuilder().append(Context, rhs.Context).append(Type, rhs.Type).append(name, rhs.name).append(Id, rhs.Id).append(affiliation, rhs.affiliation).append(jobTitle, rhs.jobTitle).append(email, rhs.email).append(URL, rhs.URL).append(codeRepository, rhs.codeRepository).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
