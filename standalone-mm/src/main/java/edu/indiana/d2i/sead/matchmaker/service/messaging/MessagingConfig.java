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
# File:  MessagingConfig.java
# Description:  Messaging service configuration.
#
# -----------------------------------------------------------------
# 
*/

package edu.indiana.d2i.sead.matchmaker.service.messaging;

import edu.indiana.d2i.sead.matchmaker.service.PropertyReader;
/**
 * @author Yuan Luo
 */
public class MessagingConfig {
	private String Username;
	private String Password;
	private String VirtualHost;
	private String Host;
	private int Port;
	private String BaseExchangeName;
	private String BaseQueueName;
	private String BaseRoutingKey;
	private String ResponseRoutingKey;
	private int MessagingRetryInterval;
	private int MessagingRetryThreshold;
	private PropertyReader property = null;
	
	public MessagingConfig(PropertyReader property){
		//this.property = PropertyReader.getInstance(propertiesPath);
		this.property = property;
		this.setUsername(this.property.getProperty("messaging.username"));
		this.setPassword(this.property.getProperty("messaging.password"));
		this.setVirtualHost(this.property.getProperty("messaging.virtualhost"));
		this.setHost(this.property.getProperty("messaging.hostname"));
		this.setPort(Integer.parseInt(this.property.getProperty("messaging.hostport")));
		this.setBaseExchangeName(this.property.getProperty("messaging.exchangename")==null? "MatchmakerExchange": this.property.getProperty("messaging.exchangename"));
		this.setBaseQueueName(this.property.getProperty("messaging.queuename")==null? "MatchmakerQueue": this.property.getProperty("messaging.queuename"));
		this.setBaseRoutingKey(this.property.getProperty("messaging.routingkey")==null? "MatchmakerKey": this.property.getProperty("messaging.routingkey"));
		this.setMessagingRetryInterval(Integer.parseInt(this.property.getProperty("messaging.retry.interval")==null? "5": this.property.getProperty("messaging.retry.interval")));
		this.setMessagingRetryThreshold(Integer.parseInt(this.property.getProperty("messaging.retry.threshold")==null? "5": this.property.getProperty("messaging.retry.threshold")));
	};
	public MessagingConfig(String propertiesPath){
		this.property = PropertyReader.getInstance(propertiesPath);
		this.setUsername(this.property.getProperty("messaging.username"));
		this.setPassword(this.property.getProperty("messaging.password"));
		this.setVirtualHost(this.property.getProperty("messaging.virtualhost"));
		this.setHost(this.property.getProperty("messaging.hostname"));
		this.setPort(Integer.parseInt(this.property.getProperty("messaging.hostport")));
		this.setBaseExchangeName(this.property.getProperty("messaging.exchangename")==null? "MatchmakerExchange": this.property.getProperty("messaging.exchangename"));
		this.setBaseQueueName(this.property.getProperty("messaging.queuename")==null? "MatchmakerQueue": this.property.getProperty("messaging.queuename"));
		this.setBaseRoutingKey(this.property.getProperty("messaging.routingkey")==null? "MatchmakerKey": this.property.getProperty("messaging.routingkey"));
		this.setMessagingRetryInterval(Integer.parseInt(this.property.getProperty("messaging.retry.interval")==null? "5": this.property.getProperty("messaging.retry.interval")));
		this.setMessagingRetryThreshold(Integer.parseInt(this.property.getProperty("messaging.retry.threshold")==null? "5": this.property.getProperty("messaging.retry.threshold")));
	};
	public MessagingConfig(String Username, String Password, String Host, int Port, String VirtualHost, String ExchangeName, String QueueName, String RoutingKey, int MessagingRetryInterval, int MessagingRetryThreshold){
		this.setUsername(Username);
		this.setPassword(Password);
		this.setVirtualHost(VirtualHost);
		this.setHost(Host);
		this.setPort(Port);
		this.setBaseExchangeName(ExchangeName);
		this.setBaseQueueName(QueueName);
		this.setBaseRoutingKey(RoutingKey);
		this.setMessagingRetryInterval(MessagingRetryInterval);
		this.setMessagingRetryThreshold(MessagingRetryThreshold);
	};
	public MessagingConfig(MessagingConfig msgconf){
		this.setUsername(msgconf.getUsername());
		this.setPassword(msgconf.getPassword());
		this.setVirtualHost(msgconf.getVirtualHost());
		this.setHost(msgconf.getHost());
		this.setPort(msgconf.getPort());
		this.setBaseExchangeName(msgconf.getBaseExchangeName());
		this.setBaseQueueName(msgconf.getBaseQueueName());
		this.setBaseRoutingKey(msgconf.getBaseRoutingKey());
		this.setMessagingRetryInterval(msgconf.getMessagingRetryInterval());
		this.setMessagingRetryThreshold(msgconf.getMessagingRetryThreshold());
	};
	
	public MessagingConfig clone(){
		return new MessagingConfig(this);
	};
	
	public void	setUsername(String Username){
		this.Username=Username;
	};
	public void	setPassword(String Password){
		this.Password=Password;
	};
	public void	setVirtualHost(String VirtualHost){
		this.VirtualHost=VirtualHost;
	};
	public void	setHost(String Host){
		this.Host=Host;
	};
	public void setPort(int Port){
		this.Port=Port;
	};
	public void	setBaseExchangeName(String ExchangeName){
		this.BaseExchangeName=ExchangeName;
	};
	public void	setBaseRoutingKey(String RoutingKey){
		this.BaseRoutingKey=RoutingKey;
	};
	public void	setBaseQueueName(String QueueName){
		this.BaseQueueName=QueueName;
	};
	public void setMessagingRetryInterval(int MessagingRetryInterval){
		this.MessagingRetryInterval=MessagingRetryInterval;
	};
	public void setMessagingRetryThreshold(int MessagingRetryThreshold){
		this.MessagingRetryThreshold=MessagingRetryThreshold;
	};
	public String getUsername(){
		return this.Username;
	};
	public String getPassword(){
		return this.Password;
	};
	public String getVirtualHost(){
		return this.VirtualHost;
	};
	public String getHost(){
		return this.Host;
	};
	public int getPort(){
		return this.Port;
	};
	public String getBaseExchangeName(){
		return this.BaseExchangeName;
	};
	public String getBaseRoutingKey(){
		return this.BaseRoutingKey;
	};
	public String getBaseQueueName(){
		return this.BaseQueueName;
	};
	public String getAsyncRequestExchangeName(){
		return this.BaseExchangeName+"_AsyncRequest";
	};
	public String getAsyncRequestRoutingKey(){
		return this.BaseRoutingKey+"_AsyncRequest";
	};
	public String getAsyncRequestQueueName(){
		return this.BaseQueueName+"_AsyncRequest";
	};
	public String getRequestExchangeName(){
		return this.BaseExchangeName+"_Request";
	};
	public String getRequestRoutingKey(){
		return this.BaseRoutingKey+"_Request";
	};
	public String getRequestQueueName(){
		return this.BaseQueueName+"_Request";
	};
	public String getResponseExchangeName(){
		return this.BaseExchangeName+"_Response";
	};
	public String getResponseQueueName(){
		return this.BaseQueueName+"_Response";
	};
	public String getResponseRoutingKey(){
		return this.ResponseRoutingKey;
	};
	public void setResponseRoutingKey(String ResponseRoutingKey){
		this.ResponseRoutingKey=ResponseRoutingKey;
	};
	
	public int getMessagingRetryInterval(){
		return this.MessagingRetryInterval;
	};
	public int getMessagingRetryThreshold(){
		return this.MessagingRetryThreshold;
	};
	

}
