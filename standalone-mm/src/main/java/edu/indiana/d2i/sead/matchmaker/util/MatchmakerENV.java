/*
#
# Copyright 2015 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: Matchmaker
# File:  AbstractENV.java
# Description: Any external abstractions, including environment variables.
#
# -----------------------------------------------------------------
# 
*/

package edu.indiana.d2i.sead.matchmaker.util;

/**
 * @author yuanluo
 * 
 * Reserved for passing through external environment variables.
 */
public class MatchmakerENV {

	/**
	 * @param args
	 */
	private PropertyReader property = null;
	private String RuleJarProfilePath;
	private String MatchmakerInputSchemaClassName;
	private String MatchmakerOutputSchemaClassName;
	private String CachedProfileRepositories;
	private String CachedProfilePerson;
	private String RepoPropertiesPath;
	
	public MatchmakerENV(PropertyReader property){
		//this.property = PropertyReader.getInstance(propertiesPath);
		this.property = property;
		this.setRuleJarProfilePath(this.property.getProperty("matchmaker.rule.jar.properties.path"));
		this.setMatchmakerInputSchemaClassName(this.property.getProperty("matchmaker.input.schema.class.name"));
		this.setMatchmakerOutputSchemaClassName(this.property.getProperty("matchmaker.output.schema.class.name"));
		this.setCachedProfileRepositories(this.property.getProperty("cached.profile.repositories"));
		this.setCachedProfilePerson(this.property.getProperty("cached.profile.person"));
		this.setRepoPropertiesPath(this.property.getProperty("repo.properties.path"));
	};
	public MatchmakerENV(String propertiesPath){
		this.property = PropertyReader.getInstance(propertiesPath);
		this.setRuleJarProfilePath(this.property.getProperty("matchmaker.rule.jar.properties.path"));
		this.setMatchmakerInputSchemaClassName(this.property.getProperty("matchmaker.input.schema.class.name"));
		this.setMatchmakerOutputSchemaClassName(this.property.getProperty("matchmaker.output.schema.class.name"));
		this.setCachedProfileRepositories(this.property.getProperty("cached.profile.repositories"));
		this.setCachedProfilePerson(this.property.getProperty("cached.profile.person"));
		this.setRepoPropertiesPath(this.property.getProperty("repo.properties.path"));
	};
	public MatchmakerENV(String RuleJarProfilePath, String MatchmakerInputSchemaClassName, String MatchmakerOutputSchemaClassName, String CachedProfileRepositories, String CachedProfilePerson, String RepoPropertiesPath){
		this.setRuleJarProfilePath(RuleJarProfilePath);
		this.setMatchmakerInputSchemaClassName(MatchmakerInputSchemaClassName);
		this.setMatchmakerOutputSchemaClassName(MatchmakerOutputSchemaClassName);
		this.setCachedProfileRepositories(CachedProfileRepositories);
		this.setCachedProfilePerson(CachedProfilePerson);
		this.setRepoPropertiesPath(RepoPropertiesPath);
	};
	public MatchmakerENV(MatchmakerENV env){
		this.setRuleJarProfilePath(env.getRuleJarProfilePath());
		this.setMatchmakerInputSchemaClassName(env.getMatchmakerInputSchemaClassName());
		this.setMatchmakerOutputSchemaClassName(env.getMatchmakerOutputSchemaClassName());
		this.setCachedProfileRepositories(env.getCachedProfileRepositories());
		this.setCachedProfilePerson(env.getCachedProfilePerson());
		this.setRepoPropertiesPath(env.getRepoPropertiesPath());

	};
	
	public void	setRuleJarProfilePath(String RuleJarProfilePath){
		this.RuleJarProfilePath=RuleJarProfilePath;
	};
	public String getRuleJarProfilePath(){
		return this.RuleJarProfilePath;
	};
	public void	setMatchmakerInputSchemaClassName(String MatchmakerInputSchemaClassName){
		this.MatchmakerInputSchemaClassName=MatchmakerInputSchemaClassName;
	};
	public String getMatchmakerInputSchemaClassName(){
		return this.MatchmakerInputSchemaClassName;
	};
	public void	setMatchmakerOutputSchemaClassName(String MatchmakerOutputSchemaClassName){
		this.MatchmakerOutputSchemaClassName=MatchmakerOutputSchemaClassName;
	};
	public String getMatchmakerOutputSchemaClassName(){
		return this.MatchmakerOutputSchemaClassName;
	};
	public void	setCachedProfileRepositories(String CachedProfileRepositories){
		this.CachedProfileRepositories=CachedProfileRepositories;
	};
	public String getCachedProfileRepositories(){
		return this.CachedProfileRepositories;
	};
	public void	setCachedProfilePerson(String CachedProfilePerson){
		this.CachedProfilePerson=CachedProfilePerson;
	};
	public String getCachedProfilePerson(){
		return this.CachedProfilePerson;
	};
	public void	setRepoPropertiesPath(String RepoPropertiesPath){
		this.RepoPropertiesPath=RepoPropertiesPath;
	};
	public String getRepoPropertiesPath(){
		return this.RepoPropertiesPath;
	};
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
