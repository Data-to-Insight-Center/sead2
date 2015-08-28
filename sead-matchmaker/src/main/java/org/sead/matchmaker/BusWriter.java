package org.sead.matchmaker;

import java.io.InputStream;
import java.util.Properties;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

public class BusWriter {
	  
	public static void main(String[] args) throws Exception {

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
		      byte[] messageBodyBytes = "Hello, world!".getBytes();
		      channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY
		,MessageProperties.PERSISTENT_TEXT_PLAIN, messageBodyBytes) ;
		      channel.close();
		      connection.close();			
	}

}
