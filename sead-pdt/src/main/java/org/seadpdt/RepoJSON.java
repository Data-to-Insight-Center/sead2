package org.seadpdt;

public class RepoJSON {
	 
	String context;
	String type;
	String orgidentifier;
	String repositoryName;
 
	public String getContext() {
		return context;
	}
 
	public void setContext(String context) {
		this.context = context;
	}
 
	public String getType() {
		return type;
	}
 
	public void setType(String type) {
		this.type = type;
	}
	
	public String getOrgIdentifier() {
		return orgidentifier;
	}
 
	public void setOrgIdentifier(String orgidentifier) {
		this.orgidentifier = orgidentifier;
	}
 
	public String getRepositoryName() {
		return repositoryName;
	}
 
	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}	
 
	@Override
	public String toString() {
		return "Repository [@context=" + context + ", @type=" + type + 
				", orgidentifier" + orgidentifier + ", repositoryName" +
				repositoryName + "]";
	}
	
}
