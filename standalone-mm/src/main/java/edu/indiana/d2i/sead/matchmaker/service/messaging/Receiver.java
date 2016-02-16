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
# File:  Receiver.java
# Description:  Message receiver.
#
# -----------------------------------------------------------------
# 
*/

package edu.indiana.d2i.sead.matchmaker.service.messaging;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
/**
 * @author Yuan Luo
 */

public class Receiver {
	
	public ConnectionFactory factory;
	public Connection conn;
	public Channel channel;
	public String ExchangeName;
	public String QueueName;
	public String RoutingKey;
	public QueueingConsumer consumer;
	
	Receiver(MessagingConfig msgconf) throws IOException{
		this.factory = new ConnectionFactory();
		this.factory.setUsername(msgconf.getUsername());
		this.factory.setPassword(msgconf.getPassword());
		this.factory.setVirtualHost(msgconf.getVirtualHost());
		this.factory.setHost(msgconf.getHost());
		this.factory.setPort(msgconf.getPort());

		this.ExchangeName = msgconf.getBaseExchangeName();
		this.QueueName = msgconf.getBaseQueueName();
		this.RoutingKey = msgconf.getBaseRoutingKey();
		createConnection();
		createChannel();
		formatChannel();
		
	};

	public Receiver(MessagingConfig msgconf, MessagingOperationTypes OpType) throws IOException{
		this.factory = new ConnectionFactory();
		this.factory.setUsername(msgconf.getUsername());
		this.factory.setPassword(msgconf.getPassword());
		this.factory.setVirtualHost(msgconf.getVirtualHost());
		this.factory.setHost(msgconf.getHost());
		this.factory.setPort(msgconf.getPort());

		switch (OpType) {
		case RECEIVE_ASYNC_REQUEST:
			this.ExchangeName = msgconf.getAsyncRequestExchangeName();
			this.QueueName = msgconf.getAsyncRequestQueueName();
			this.RoutingKey = msgconf.getAsyncRequestRoutingKey();
			break;
		case RECEIVE_REQUESTS:
			this.ExchangeName = msgconf.getRequestExchangeName();
			this.QueueName = msgconf.getRequestQueueName();
			this.RoutingKey = msgconf.getRequestRoutingKey();
			break;
		case RECEIVE_RESPONSE:
			this.ExchangeName = msgconf.getResponseExchangeName();
			this.QueueName = msgconf.getResponseQueueName();
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
		
		createConnection();
		createChannel();
		formatChannel();
		
	};
	public void createConnection() throws IOException{
		this.conn = this.factory.newConnection();
	}
	public void createChannel() throws IOException{
		this.channel = this.conn.createChannel();
	}
	public void formatChannel() throws IOException{
		
		boolean durable = true;
		this.channel.exchangeDeclare(this.ExchangeName, "direct", durable);
		this.channel.queueDeclare(this.QueueName, durable,false,false,null);
		this.channel.queueBind(this.QueueName, this.ExchangeName, this.RoutingKey);
		boolean noAck = false;
		this.consumer = new QueueingConsumer(this.channel);
		this.channel.basicConsume(this.QueueName, noAck, this.consumer);
	}
	
	public void abortChannel(){
		try {
			this.channel.abort();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void closeChannel(){
		try {
			if(this.channel.isOpen()){
				this.channel.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void abortConnection(){
		this.conn.abort();
	}
	public void closeConnection(){
		try {
			this.closeChannel();
			if(this.conn.isOpen()){
				this.conn.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public String getMessage() throws IOException, ShutdownSignalException, InterruptedException{
		QueueingConsumer.Delivery delivery;
		delivery = this.consumer.nextDelivery();
		String message= new String(delivery.getBody());
		channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
		return message;
		
	};

}
