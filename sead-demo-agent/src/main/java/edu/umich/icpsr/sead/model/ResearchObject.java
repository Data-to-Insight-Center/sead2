package edu.umich.icpsr.sead.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResearchObject {

	@JsonProperty("@id")
	private String id;

	@JsonProperty("@type")
	private List<String> type;

	@JsonProperty("http://purl.org/dc/elements/1.1/creator")
	private String creator;

	@JsonProperty("http://purl.org/dc/elements/1.1/identifier")
	private String identifier;

	@JsonProperty("http://purl.org/dc/elements/1.1/title")
	private String title;

	@JsonProperty("http://purl.org/dc/terms/abstract")
	private String abstractText;

	@JsonProperty("http://purl.org/dc/terms/created")
	private String created;

	@JsonProperty("http://purl.org/dc/terms/creator")
	@JsonDeserialize(using = StringOrListValueDes.class)
	private List<String> authors;

	@JsonProperty("http://purl.org/dc/terms/hasPart")
	private List<String> hasPart;

	@JsonProperty("http://purl.org/dc/terms/isVersionOf")
	private String isVersionOf;

	@JsonProperty("http://sead-data.net/terms/ProposedForPublication")
	private String proposedForPulication;

	@JsonProperty("http://www.openarchives.org/ore/terms/aggregates")
	private List<ResearchDataSet> aggregates;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<String> getType() {
		return type;
	}

	public void setType(List<String> type) {
		this.type = type;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAbstractText() {
		return abstractText;
	}

	public void setAbstractText(String abstractText) {
		this.abstractText = abstractText;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public List<String> getHasPart() {
		return hasPart;
	}

	public void setHasPart(List<String> hasPart) {
		this.hasPart = hasPart;
	}

	public String getIsVersionOf() {
		return isVersionOf;
	}

	public void setIsVersionOf(String isVersionOf) {
		this.isVersionOf = isVersionOf;
	}

	public String getProposedForPulication() {
		return proposedForPulication;
	}

	public void setProposedForPulication(String proposedForPulication) {
		this.proposedForPulication = proposedForPulication;
	}

	public List<ResearchDataSet> getAggregates() {
		return aggregates;
	}

	public void setAggregates(List<ResearchDataSet> aggregates) {
		this.aggregates = aggregates;
	}

}
