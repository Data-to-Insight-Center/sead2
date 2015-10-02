package org.sead.sda.agent.calls;

import java.util.Properties;

import org.sead.sda.agent.engine.PropertiesReader;

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
