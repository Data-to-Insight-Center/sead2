package org.sead.matchmaker;

import com.rabbitmq.client.*;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.InputStream;

public class BusWriter {
	  
	public static void main(String[] args) throws Exception {

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
		      byte[] messageBodyBytes = "Hello, world!".getBytes();
		      channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY
		,MessageProperties.PERSISTENT_TEXT_PLAIN, messageBodyBytes) ;
		      channel.close();
		      connection.close();			
	}

}
