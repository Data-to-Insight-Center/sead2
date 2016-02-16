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
public class AsynchronizedClient {
	private Sender sender;
	private Receiver receiver;
	private MessagingConfig msgconf;
	
	/**
	 * 
	 * @param msgconf
	 */
	public AsynchronizedClient(MessagingConfig msgconf){
		try {
			this.msgconf=msgconf;
			this.sender=new Sender(msgconf,MessagingOperationTypes.SEND_ASYNC_REQUEST);
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
	public void sendRequest(JsonNode JosonRequest) {
		
		String request=JosonRequest.toString();
		try {
			//send message
			this.sender.sendMessage("{\"request\":"+request+"}");
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
		
	}
	/**
	 * Send a request (described in a json string) to Matchmaker Server through Message Bus.
	 * @param request
	 */
	public void sendRequest(String request){
		//TODO: Validate Message Format
    	String ResponseRoutingKey=UUID.randomUUID().toString();
		try {
			//send message
			this.sender.sendMessage("{\"request\":"+request+"}");

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

		
	}
	/**
	 * Send a request (described in a file) to Matchmaker Server through Message Bus.
	 * @param RequestFile
	 * @throws IOException
	 */
	public void sendRequest(File RequestFile) throws IOException{
		ObjectMapper mapper = new ObjectMapper();
		 
		try {
			JsonNode rootNode = mapper.readTree(RequestFile);
			//String ResponseRoutingKey=UUID.randomUUID().toString();
			String request=rootNode.toString();
			try {
				//send message
				this.sender.sendMessage("{\"request\":"+request+"}");
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

	}
	
	
	/**
	 * Close Messaging Channel
	 */
	public void closeChannel(){
		this.sender.closeChannel();
	}
	/**
	 * Close Messaging Channel and Close Message Bus Connection
	 */
	public void closeConnection(){
		this.sender.closeConnection();
	}

	public boolean request(String Query){
		try {
			this.sendRequest(Query);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true; 
	}
	
	public boolean request(JsonNode JsonRequest){
		try {
			this.sendRequest(JsonRequest);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true; 
	}
	
	public boolean request(File RequestFile){
		try {
			this.sendRequest(RequestFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true; 
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
		AsynchronizedClient asynchronizedClient=new AsynchronizedClient(msgconf);
		boolean result=asynchronizedClient.request(new File(pathToQueryFile));
		asynchronizedClient.closeConnection();
		
		//System.out.println("Result:\n"+result);
		System.out.println(result);
		
		//long endTime = System.currentTimeMillis();
		//System.out.println("Total Execution Time: "+(endTime-startTime));
		
		
	

	}

}
