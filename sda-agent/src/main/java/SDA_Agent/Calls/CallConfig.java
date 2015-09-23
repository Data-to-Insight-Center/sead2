package Calls;

import java.util.Properties;

import Engine.PropertiesReader;

public class CallConfig {
	
	private int daemons;
	private Properties properties;
	
	
	public CallConfig(String propertiesPath){
		
		this.properties = new PropertiesReader(propertiesPath).getProperties();
		
		this.daemons = Integer.parseInt(this.properties.getProperty("call.daemon"));
		
	}
	
	
	public int getDaemon(){
		return this.daemons;
	}

}
