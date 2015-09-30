package edu.umich.icpsr.sead.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResearchObjectOreMap {
	@JsonProperty("@id")
	private String id;

	@JsonProperty("@type")
	private String type;

	@JsonProperty("http://cet.ncsa.uiuc.edu/2007/mmdb/describes")
	private ResearchObject describes;

	@JsonProperty("http://purl.org/dc/terms/created")
	private String created;

	@JsonProperty("http://purl.org/dc/terms/creator")
	@JsonDeserialize(using = StringOrListValueDes.class)
	private List<String> creator;

	@JsonProperty("http://purl.org/dc/terms/rights")
	private String rights;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ResearchObject getDescribes() {
		return describes;
	}

	public void setDescribes(ResearchObject describes) {
		this.describes = describes;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public List<String> getCreator() {
		return creator;
	}

	public void setCreator(List<String> creator) {
		this.creator = creator;
	}

	public String getRights() {
		return rights;
	}

	public void setRights(String rights) {
		this.rights = rights;
	}

}
