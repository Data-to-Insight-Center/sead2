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
# Project:		Matchmaker Messaging Client
# File:			SynchronizedClient.java
# Description:	API for sending messages to Matchmaker service, 
#				through messaging bus.  
#
# -----------------------------------------------------------------
# 
*/
package edu.indiana.d2i.sead.matchmaker.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.ShutdownSignalException;

import edu.indiana.d2i.sead.matchmaker.service.messaging.MessagingConfig;
import edu.indiana.d2i.sead.matchmaker.service.messaging.MessagingOperationTypes;
import edu.indiana.d2i.sead.matchmaker.service.messaging.MessagingQueue;
import edu.indiana.d2i.sead.matchmaker.service.messaging.Receiver;
import edu.indiana.d2i.sead.matchmaker.service.messaging.Sender;


/**
 * @author Yuan Luo (yuanluo@indiana.edu)
 *
 */
public class SynchronizedClient {
	private Sender sender;
	private Receiver receiver;
	private MessagingConfig msgconf;
	
	/**
	 * 
	 * @param msgconf
	 */
	public SynchronizedClient(MessagingConfig msgconf){
		try {
			this.msgconf=msgconf;
			this.sender=new Sender(msgconf,MessagingOperationTypes.SEND_REQUEST);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Send Query to Server through Message Bus.
	 * @param SynchronizedClient
	 * @throws XmlException
	 */
	public String sendRequest(JsonNode JosonRequest) {
		
		String ResponseRoutingKey=UUID.randomUUID().toString();
		String request=JosonRequest.toString();
		String temporaryQueueBase=ResponseRoutingKey;
		this.msgconf.setBaseQueueName(temporaryQueueBase);
		try {
			//Bind response exchange-queue-routingkey before send request.
			new MessagingQueue().new QueueBind(this.msgconf, this.msgconf.getResponseExchangeName(), this.msgconf.getResponseQueueName(), ResponseRoutingKey);
			//send message
			this.sender.sendMessage("{\"requestID\":\""+ResponseRoutingKey+"\",\"responseKey\":\""+ResponseRoutingKey+"\",\"request\":"+request+"}");
			return ResponseRoutingKey;
		} catch (ShutdownSignalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	/**
	 * Send a request (described in a json string) to Matchmaker Server through Message Bus.
	 * @param request
	 */
	public String sendRequest(String request){
		//TODO: Validate Message Format
    	String ResponseRoutingKey=UUID.randomUUID().toString();
    	String temporaryQueueBase=ResponseRoutingKey;
		this.msgconf.setBaseQueueName(temporaryQueueBase);
		try {
			//Bind response exchange-queue-routingkey before send request.
			new MessagingQueue().new QueueBind(this.msgconf, this.msgconf.getResponseExchangeName(), this.msgconf.getResponseQueueName(), ResponseRoutingKey);
			//send message
			this.sender.sendMessage("{\"requestID\":\""+ResponseRoutingKey+"\",\"responseKey\":\""+ResponseRoutingKey+"\",\"request\":"+request+"}");
			return ResponseRoutingKey;
		} catch (ShutdownSignalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return null;
	}
	/**
	 * Send a request (described in a file) to Matchmaker Server through Message Bus.
	 * @param RequestFile
	 * @throws IOException
	 */
	public String sendRequest(File RequestFile) throws IOException{
		ObjectMapper mapper = new ObjectMapper();
		 
		try {
			JsonNode rootNode = mapper.readTree(RequestFile);
			String ResponseRoutingKey=UUID.randomUUID().toString();
			String temporaryQueueBase=ResponseRoutingKey;
			this.msgconf.setBaseQueueName(temporaryQueueBase);
			String request=rootNode.toString();
			try {
				//Bind response exchange-queue-routingkey before send request.
				new MessagingQueue().new QueueBind(this.msgconf, this.msgconf.getResponseExchangeName(), this.msgconf.getResponseQueueName(), ResponseRoutingKey);
				//send message
				this.sender.sendMessage("{\"requestID\":\""+ResponseRoutingKey+"\",\"responseKey\":\""+ResponseRoutingKey+"\",\"request\":"+request+"}");
				return ResponseRoutingKey;
			} catch (ShutdownSignalException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 	
	 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Receive Response from Matchmaker Server via Message Bus.
	 * @param QueryFile
	 * @throws IOException
	 */
	public String receiveResponse(String ResponseRoutingKey) throws IOException{
		
		try {
			this.msgconf.setResponseRoutingKey(ResponseRoutingKey);
			String temporaryQueueBase=ResponseRoutingKey;
			this.msgconf.setBaseQueueName(temporaryQueueBase);
			this.receiver=new Receiver(this.msgconf,MessagingOperationTypes.RECEIVE_RESPONSE);
			//System.out.println(msgconf.getResponseExchangeName()+":"+msgconf.getResponseQueueName()+":"+msgconf.getResponseRoutingKey());
			String response=this.receiver.getMessage();
			this.receiver.closeConnection();
			//Unbind after receiving message
			new MessagingQueue().new QueueUnBind(this.msgconf, this.msgconf.getResponseExchangeName(), this.msgconf.getResponseQueueName(), ResponseRoutingKey);
			return response;
			
		} catch (ShutdownSignalException e) {
			e.printStackTrace();
				
			this.receiver.abortChannel();
			this.receiver.abortConnection();
			
			boolean reconnected=false;
			int retry_count=0;
			while(!reconnected){
				if(retry_count>3){
					return null;
				}
				retry_count++;
				reconnected=false;
				try {
					this.receiver.createConnection();
					this.receiver.createChannel();
					reconnected=true;
				} catch (IOException e1) {
					reconnected=false;
					e1.printStackTrace();
				}
				//break the while loop when reconnected
				if(reconnected){
					try {
						this.receiver.formatChannel();
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
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * Close Messaging Channel
	 */
	public void closeChannel(){
		this.sender.closeChannel();
		this.receiver.closeChannel();
	}
	/**
	 * Close Messaging Channel and Close Message Bus Connection
	 */
	public void closeConnection(){
		this.sender.closeConnection();
		this.receiver.closeConnection();
	}

	public String request(String Query){
		String response=new String();
		try {
			String responseRoutingKey=this.sendRequest(Query);
			//System.out.println("responseRoutingKey: "+responseRoutingKey);
			response= this.receiveResponse(responseRoutingKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response; 
	}
	
	public String request(JsonNode JsonRequest){
		String response=new String();
		try {
			String responseRoutingKey=this.sendRequest(JsonRequest);
			//System.out.println("responseRoutingKey: "+responseRoutingKey);
			response= this.receiveResponse(responseRoutingKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response; 
	}
	
	public String request(File RequestFile){
		String response=new String();
		try {
			String responseRoutingKey=this.sendRequest(RequestFile);
			//System.out.println("responseRoutingKey: "+responseRoutingKey);
			response= this.receiveResponse(responseRoutingKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response; 
	}
	
	public static void main(String[] args) {
		if(args.length!=2){
			System.out.println("Query:\nargs[0]: Message Configuration File.\nargs[1]: Request in JSON file format.");
			System.out.println("Please check the parameters.");
			return;
		}
		String MessageConfigPath=args[0];
		String pathToQueryFile=args[1];
		long startTime = System.currentTimeMillis();
		MessagingConfig msgconf=new MessagingConfig(MessageConfigPath);
		SynchronizedClient synchronizedClient=new SynchronizedClient(msgconf);
		String result=synchronizedClient.request(new File(pathToQueryFile));
		synchronizedClient.closeConnection();
		
		//System.out.println("Result:\n"+result);
		System.out.println(result);
		
		//long endTime = System.currentTimeMillis();
		//System.out.println("Total Execution Time: "+(endTime-startTime));
		
		
	

	}

}
