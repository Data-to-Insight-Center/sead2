package org.sead.matchmaker;

import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class BusListener {

	  
	public static void main(String[] args) throws Exception, TimeoutException {
				
		ConnectionFactory factory = new ConnectionFactory();
		Properties prop = new Properties();
		
        InputStream input =
                APIServices.class.getResourceAsStream("config.properties");		
		prop.load(input);
		
		String EXCHANGE_NAME = prop.getProperty("messaging.exchangename");
		String QUEUE_NAME = prop.getProperty("messaging.queuename");
		String ROUTING_KEY = prop.getProperty("messaging.routingkey");
	    factory.setUsername(prop.getProperty("messaging.username"));
	    factory.setPassword(prop.getProperty("messaging.password"));
	    factory.setVirtualHost(prop.getProperty("messaging.virtualhost"));
	    factory.setHost(prop.getProperty("messaging.hostname"));
	    factory.setPort(Integer.parseInt(prop.getProperty("messaging.hostport")));
		 
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel(); 
	    boolean durable = true;
	    channel.queueDeclare(QUEUE_NAME, durable, false, false, null);
	    
		      channel.exchangeDeclare(EXCHANGE_NAME, "direct", durable);
		      channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
		      boolean noAck = false;
		      QueueingConsumer consumer = new QueueingConsumer(channel);
		      channel.basicConsume(QUEUE_NAME, noAck, consumer);
		      boolean runInfinite = true;
		
		      System.out.println("listening for messages on queue: " + QUEUE_NAME);
		      while (runInfinite) {
		            QueueingConsumer.Delivery delivery;
		            try {
		               delivery = consumer.nextDelivery();
		            } catch (InterruptedException ie) {
		               continue;
		            }
		         System.out.println("Message received: " 
		+ new String(delivery.getBody()));
		         channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
		      }		      
		      channel.close();
		      connection.close();		      
	}

}
