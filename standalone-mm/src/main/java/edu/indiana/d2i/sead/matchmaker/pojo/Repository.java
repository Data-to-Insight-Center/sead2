
package edu.indiana.d2i.sead.matchmaker.pojo;

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "@context",
    "@type",
    "orgidentifier",
    "repositoryName",
    "repositoryURL",
    "institution",
    "subject",
    "versioning",
    "dataAccessType",
    "dataLicenseName",
    "contentType",
    "/maxFileSize"
})
public class Repository {

    @JsonProperty("@context")
    private String Context;
    @JsonProperty("@type")
    private String Type;
    @JsonProperty("orgidentifier")
    private String orgidentifier;
    @JsonProperty("repositoryName")
    private String repositoryName;
    @JsonProperty("repositoryURL")
    private String repositoryURL;
    @JsonProperty("institution")
    private String institution;
    @JsonProperty("subject")
    private String subject;
    @JsonProperty("versioning")
    private String versioning;
    @JsonProperty("dataAccessType")
    private List<String> dataAccessType = new ArrayList<String>();
    @JsonProperty("dataLicenseName")
    private List<String> dataLicenseName = new ArrayList<String>();
    @JsonProperty("contentType")
    private List<String> contentType = new ArrayList<String>();
    @JsonProperty("/maxFileSize")
    private edu.indiana.d2i.sead.matchmaker.pojo.MaxFileSize MaxFileSize;
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
     *     The orgidentifier
     */
    @JsonProperty("orgidentifier")
    public String getOrgidentifier() {
        return orgidentifier;
    }

    /**
     * 
     * @param orgidentifier
     *     The orgidentifier
     */
    @JsonProperty("orgidentifier")
    public void setOrgidentifier(String orgidentifier) {
        this.orgidentifier = orgidentifier;
    }

    /**
     * 
     * @return
     *     The repositoryName
     */
    @JsonProperty("repositoryName")
    public String getRepositoryName() {
        return repositoryName;
    }

    /**
     * 
     * @param repositoryName
     *     The repositoryName
     */
    @JsonProperty("repositoryName")
    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    /**
     * 
     * @return
     *     The repositoryURL
     */
    @JsonProperty("repositoryURL")
    public String getRepositoryURL() {
        return repositoryURL;
    }

    /**
     * 
     * @param repositoryURL
     *     The repositoryURL
     */
    @JsonProperty("repositoryURL")
    public void setRepositoryURL(String repositoryURL) {
        this.repositoryURL = repositoryURL;
    }

    /**
     * 
     * @return
     *     The institution
     */
    @JsonProperty("institution")
    public String getInstitution() {
        return institution;
    }

    /**
     * 
     * @param institution
     *     The institution
     */
    @JsonProperty("institution")
    public void setInstitution(String institution) {
        this.institution = institution;
    }

    /**
     * 
     * @return
     *     The subject
     */
    @JsonProperty("subject")
    public String getSubject() {
        return subject;
    }

    /**
     * 
     * @param subject
     *     The subject
     */
    @JsonProperty("subject")
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * 
     * @return
     *     The versioning
     */
    @JsonProperty("versioning")
    public String getVersioning() {
        return versioning;
    }

    /**
     * 
     * @param versioning
     *     The versioning
     */
    @JsonProperty("versioning")
    public void setVersioning(String versioning) {
        this.versioning = versioning;
    }

    /**
     * 
     * @return
     *     The dataAccessType
     */
    @JsonProperty("dataAccessType")
    public List<String> getDataAccessType() {
        return dataAccessType;
    }

    /**
     * 
     * @param dataAccessType
     *     The dataAccessType
     */
    @JsonProperty("dataAccessType")
    public void setDataAccessType(List<String> dataAccessType) {
        this.dataAccessType = dataAccessType;
    }

    /**
     * 
     * @return
     *     The dataLicenseName
     */
    @JsonProperty("dataLicenseName")
    public List<String> getDataLicenseName() {
        return dataLicenseName;
    }

    /**
     * 
     * @param dataLicenseName
     *     The dataLicenseName
     */
    @JsonProperty("dataLicenseName")
    public void setDataLicenseName(List<String> dataLicenseName) {
        this.dataLicenseName = dataLicenseName;
    }

    /**
     * 
     * @return
     *     The contentType
     */
    @JsonProperty("contentType")
    public List<String> getContentType() {
        return contentType;
    }

    /**
     * 
     * @param contentType
     *     The contentType
     */
    @JsonProperty("contentType")
    public void setContentType(List<String> contentType) {
        this.contentType = contentType;
    }

    /**
     * 
     * @return
     *     The MaxFileSize
     */
    @JsonProperty("/maxFileSize")
    public edu.indiana.d2i.sead.matchmaker.pojo.MaxFileSize getMaxFileSize() {
        return MaxFileSize;
    }

    /**
     * 
     * @param MaxFileSize
     *     The /maxFileSize
     */
    @JsonProperty("/maxFileSize")
    public void setMaxFileSize(edu.indiana.d2i.sead.matchmaker.pojo.MaxFileSize MaxFileSize) {
        this.MaxFileSize = MaxFileSize;
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
        return new HashCodeBuilder().append(Context).append(Type).append(orgidentifier).append(repositoryName).append(repositoryURL).append(institution).append(subject).append(versioning).append(dataAccessType).append(dataLicenseName).append(contentType).append(MaxFileSize).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Repository) == false) {
            return false;
        }
        Repository rhs = ((Repository) other);
        return new EqualsBuilder().append(Context, rhs.Context).append(Type, rhs.Type).append(orgidentifier, rhs.orgidentifier).append(repositoryName, rhs.repositoryName).append(repositoryURL, rhs.repositoryURL).append(institution, rhs.institution).append(subject, rhs.subject).append(versioning, rhs.versioning).append(dataAccessType, rhs.dataAccessType).append(dataLicenseName, rhs.dataLicenseName).append(contentType, rhs.contentType).append(MaxFileSize, rhs.MaxFileSize).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
