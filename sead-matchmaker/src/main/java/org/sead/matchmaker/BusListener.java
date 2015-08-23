package org.sead.matchmaker;

import java.util.concurrent.TimeoutException;
import com.rabbitmq.client.*;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.InputStream;

public class BusListener {

	  
	public static void main(String[] args) throws Exception, TimeoutException {

		String workingDir = System.getProperty("user.dir");
		System.out.println("Current working directory : " + workingDir);
		
		Properties prop = new Properties();
		InputStream input = null;		
		
		input = new FileInputStream("config.properties");
		prop.load(input);
		
	    ConnectionFactory factory = new ConnectionFactory();

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
