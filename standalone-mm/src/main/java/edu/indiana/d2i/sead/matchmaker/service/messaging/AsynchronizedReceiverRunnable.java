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
# File:  SynchronizedReceiverRunnable.java
# Description:  Runnable implementation of a service daemon thread.
#
# -----------------------------------------------------------------
# 
*/

package edu.indiana.d2i.sead.matchmaker.service.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.rabbitmq.client.ShutdownSignalException;
import edu.indiana.d2i.sead.matchmaker.core.POJOGenerator;
import edu.indiana.d2i.sead.matchmaker.service.MatchmakerOperations;
import edu.indiana.d2i.sead.matchmaker.service.ServiceLauncher;
import edu.indiana.d2i.sead.matchmaker.util.MatchmakerENV;
import org.apache.log4j.Logger;

import java.io.IOException;


/**
 * @author Yuan Luo
 */

public class AsynchronizedReceiverRunnable  implements Runnable  {

	private MessagingConfig msgconf;
	private MatchmakerENV env;
	private Receiver receiver;
	private MatchmakerOperations mmOperations;
	private Logger log;
	private POJOGenerator input;
	private int RETRY_INTERVAL;
	private int RETRY_THRESHOLD;
	public static final String  INVALID_REQUEST_ERROR_STRING = "Invalid Request";
	public static final String  PROCESSING_ERROR_STRING = "Processing Error";
	
	public AsynchronizedReceiverRunnable(MessagingConfig msgconf, MatchmakerENV env) throws IOException, ClassNotFoundException{
		this.msgconf=msgconf;
		this.env = env;
		this.receiver=new Receiver(msgconf, MessagingOperationTypes.RECEIVE_ASYNC_REQUEST);
		
		this.mmOperations = new MatchmakerOperations();

		this.log = Logger.getLogger(AsynchronizedReceiverRunnable.class);
		this.RETRY_INTERVAL=msgconf.getMessagingRetryInterval();
		this.RETRY_THRESHOLD=msgconf.getMessagingRetryThreshold();
		//input = new POJOGenerator("edu.indiana.d2i.sead.matchmaker.service.messaging.MatchmakerInputSchema");
		input = new POJOGenerator(env.getMatchmakerInputSchemaClassName());
	}
	
	public void run() throws java.lang.IllegalMonitorStateException{
		boolean runInfinite=true;
		String requestMessage;
		while (runInfinite) {
			
			try {
				log.info("[Matchmaker server: Listening Async Reqeusts from Messaging System]");
				requestMessage=this.receiver.getMessage();
				log.info("[Matchmaker server: One Async Reqeusts received]\n"+requestMessage);
				// Parse the Message
				if(requestMessage!=null){
					
					this.input.fromString(requestMessage);
					JsonNode requestMessageJsonNode=this.input.getJsonTree();
					
					
					//String ResponseRoutingKey=requestMessageJsonNode.get("responseKey").asText();
					JsonNode request=requestMessageJsonNode.get("request");
					log.info("[Matchmaker server: Request] "+request);
					//log.info("[Matchmaker server: Message Response Routing Key] "+ResponseRoutingKey);
					
					//Perform Service Logic
					String result=null;
					try{
						result=this.mmOperations.exec(env, request, null);
						log.info("[Matchmaker server: Async result] "+result);
						
					}catch(Exception e){
						result=PROCESSING_ERROR_STRING;
						log.info("[Matchmaker server: Processing Error] "+e.toString());
					}
					

				}else {
					log.info("[Matchmaker server: Empty Request]\n");
				}
				
				
			} catch (ShutdownSignalException e) {
				e.printStackTrace();
					
				this.receiver.abortChannel();
				this.receiver.abortConnection();
				
				boolean reconnected=false;
				int retry_count=0;
				while(!reconnected){
					if(retry_count>this.RETRY_THRESHOLD){
						ServiceLauncher.shutdown();
						return;
					}
					retry_count++;
					reconnected=false;
					try {
						log.info("Reconneting to Messaging Server.");
						this.receiver.createConnection();
						this.receiver.createChannel();
						reconnected=true;
					} catch (IOException e1) {
						log.error("Can't connect to Messaging Server.");
						reconnected=false;
						e1.printStackTrace();
					}
					//Sleep 5*retry_count seconds and try to reconnect again
					try {
						Thread.sleep(this.RETRY_INTERVAL*1000);
					} catch (InterruptedException e3) {
						e3.printStackTrace();
					}
					//break the while loop when reconnected
					if(reconnected){
						try {
							this.receiver.formatChannel();
							log.info("Reconneted to Messaging Server.");
							break;
						} catch (IOException e1) {
							reconnected=false;
							this.receiver.closeChannel();
							this.receiver.closeConnection();
						}
						
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.error("", e);
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				log.error("", e);
				e.printStackTrace();
			} 
			catch (Exception e) {
				// TODO Auto-generated catch block
				log.error("", e);
				e.printStackTrace();
			}

		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}