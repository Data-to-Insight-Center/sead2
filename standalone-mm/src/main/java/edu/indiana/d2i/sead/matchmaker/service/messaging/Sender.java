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
# File:  Sender.java
# Description:  Message sender.
#
# -----------------------------------------------------------------
# 
*/

package edu.indiana.d2i.sead.matchmaker.service.messaging;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import com.rabbitmq.client.ShutdownSignalException;

/**
 * @author Yuan Luo
 */

public class Sender {
	
	public ConnectionFactory factory;
	public Connection conn;
	public Channel channel;
	public String ExchangeName;
	public String QueueName;
	public String RoutingKey;
	
	public Sender(MessagingConfig msgconf) throws IOException{
		this.factory = new ConnectionFactory();
		this.factory.setUsername(msgconf.getUsername());
		this.factory.setPassword(msgconf.getPassword());
		this.factory.setVirtualHost(msgconf.getVirtualHost());
		this.factory.setHost(msgconf.getHost());
		this.factory.setPort(msgconf.getPort());
		
		this.conn = this.factory.newConnection();
		this.channel = this.conn.createChannel();
		this.ExchangeName = msgconf.getBaseExchangeName();
		this.RoutingKey = msgconf.getBaseRoutingKey();
		
	};	
	public Sender(MessagingConfig msgconf, MessagingOperationTypes OpType) throws IOException{
		this.factory = new ConnectionFactory();
		this.factory.setUsername(msgconf.getUsername());
		this.factory.setPassword(msgconf.getPassword());
		this.factory.setVirtualHost(msgconf.getVirtualHost());
		this.factory.setHost(msgconf.getHost());
		this.factory.setPort(msgconf.getPort());
		this.conn = this.factory.newConnection();
		this.channel = this.conn.createChannel();
		
		switch (OpType) {
		case SEND_ASYNC_REQUEST:
			this.ExchangeName = msgconf.getAsyncRequestExchangeName();
			this.RoutingKey = msgconf.getAsyncRequestRoutingKey();
			break;
		case SEND_REQUEST:
			this.ExchangeName = msgconf.getRequestExchangeName();
			this.RoutingKey = msgconf.getRequestRoutingKey();
			break;
		case SEND_RESPONSE:
			this.ExchangeName = msgconf.getResponseExchangeName();
			this.RoutingKey = msgconf.getResponseRoutingKey();
			break;
		default:
			try {
				throw new Exception("OperationType: "+OpType.toString()+" not supported.");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
		
	};
	
	public void closeChannel(){
		try {
			if(this.channel.isOpen()){
				this.channel.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void closeConnection(){
		try {
			this.closeChannel();
			if(this.conn.isOpen()){
				this.conn.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public void sendMessage(String message) throws IOException, ShutdownSignalException, InterruptedException{
		byte[] messageBodyBytes = message.getBytes();
		this.channel.basicPublish(this.ExchangeName, this.RoutingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, messageBodyBytes) ;
		
	};
	
	public void sendMessage(File messageFile) throws IOException, ShutdownSignalException, InterruptedException{
		InputStream is = new FileInputStream(messageFile);

	    // Get the size of the file
	    long length = messageFile.length();
	    if (length > Integer.MAX_VALUE) {
	    	throw new IOException("Input File ("+messageFile.getName()+") is to large! ");
	    }
	    byte[] messageBodyBytes = new byte[(int)length];
	    int offset = 0;
	    int numRead = 0;
	    while (offset < messageBodyBytes.length
	           && (numRead=is.read(messageBodyBytes, offset, messageBodyBytes.length-offset)) >= 0) {
	        offset += numRead;
	    }
	    if (offset < messageBodyBytes.length) {
	        throw new IOException("Could not completely read file "+messageFile.getName());
	    }
	    is.close();
		this.channel.basicPublish(this.ExchangeName, this.RoutingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, messageBodyBytes) ;
		
	};
	
	public static void main(String[] args) throws ShutdownSignalException, IOException, InterruptedException {
    		MessagingConfig msgconf=new MessagingConfig("/Users/yuanluo/WorkZone/workspace/MatchMaker/config/server.properties");
    		Sender sender=new Sender(msgconf, MessagingOperationTypes.SEND_REQUEST);
    		System.out.println(sender.RoutingKey);
    		File messageFile=new File("/Users/yuanluo/WorkZone/workspace/MatchMaker/samples/research_object.json");
    		sender.sendMessage(messageFile);
    		sender.closeConnection();
		

	}

}