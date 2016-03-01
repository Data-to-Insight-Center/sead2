
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
    "description",
    "sourceOrganization",
    "author",
    "fileSize",
    "contentUrl",
    "/subject",
    "contentType"
})
public class ResearchObject {

    @JsonProperty("@context")
    private String Context;
    @JsonProperty("@type")
    private String Type;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("sourceOrganization")
    private String sourceOrganization;
    @JsonProperty("author")
    private Author author;
    @JsonProperty("fileSize")
    private FileSize fileSize;
    @JsonProperty("contentUrl")
    private String contentUrl;
    @JsonProperty("/subject")
    private String Subject;
    @JsonProperty("contentType")
    private String contentType;
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
     *     The description
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * 
     * @param description
     *     The description
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 
     * @return
     *     The sourceOrganization
     */
    @JsonProperty("sourceOrganization")
    public String getSourceOrganization() {
        return sourceOrganization;
    }

    /**
     * 
     * @param sourceOrganization
     *     The sourceOrganization
     */
    @JsonProperty("sourceOrganization")
    public void setSourceOrganization(String sourceOrganization) {
        this.sourceOrganization = sourceOrganization;
    }

    /**
     * 
     * @return
     *     The author
     */
    @JsonProperty("author")
    public Author getAuthor() {
        return author;
    }

    /**
     * 
     * @param author
     *     The author
     */
    @JsonProperty("author")
    public void setAuthor(Author author) {
        this.author = author;
    }

    /**
     * 
     * @return
     *     The fileSize
     */
    @JsonProperty("fileSize")
    public FileSize getFileSize() {
        return fileSize;
    }

    /**
     * 
     * @param fileSize
     *     The fileSize
     */
    @JsonProperty("fileSize")
    public void setFileSize(FileSize fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * 
     * @return
     *     The contentUrl
     */
    @JsonProperty("contentUrl")
    public String getContentUrl() {
        return contentUrl;
    }

    /**
     * 
     * @param contentUrl
     *     The contentUrl
     */
    @JsonProperty("contentUrl")
    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    /**
     * 
     * @return
     *     The Subject
     */
    @JsonProperty("/subject")
    public String getSubject() {
        return Subject;
    }

    /**
     * 
     * @param Subject
     *     The /subject
     */
    @JsonProperty("/subject")
    public void setSubject(String Subject) {
        this.Subject = Subject;
    }

    /**
     * 
     * @return
     *     The contentType
     */
    @JsonProperty("contentType")
    public String getContentType() {
        return contentType;
    }

    /**
     * 
     * @param contentType
     *     The contentType
     */
    @JsonProperty("contentType")
    public void setContentType(String contentType) {
        this.contentType = contentType;
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
        return new HashCodeBuilder().append(Context).append(Type).append(name).append(description).append(sourceOrganization).append(author).append(fileSize).append(contentUrl).append(Subject).append(contentType).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResearchObject) == false) {
            return false;
        }
        ResearchObject rhs = ((ResearchObject) other);
        return new EqualsBuilder().append(Context, rhs.Context).append(Type, rhs.Type).append(name, rhs.name).append(description, rhs.description).append(sourceOrganization, rhs.sourceOrganization).append(author, rhs.author).append(fileSize, rhs.fileSize).append(contentUrl, rhs.contentUrl).append(Subject, rhs.Subject).append(contentType, rhs.contentType).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
