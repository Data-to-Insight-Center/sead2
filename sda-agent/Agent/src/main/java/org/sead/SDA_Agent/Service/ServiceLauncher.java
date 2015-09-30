package Service;

import org.apache.log4j.Logger;

import Calls.CallConfig;
import Calls.CallDaemons;

public class ServiceLauncher {
	
	private static String propertiesPath;
	
	private static boolean onOf = false;
	
	private static final Logger log = Logger.getLogger(ServiceLauncher.class);
	

	public static void start(String propertyPath){
		if (!onOf){
			propertiesPath = propertyPath;
			
			if (log.isDebugEnabled()){
				log.debug("SDA Agent started");
			}
			
			onOf = true;
		}
	}
	
	
	
	public static void shutDown(){
		if (onOf){
			
			if (log.isDebugEnabled()){
				log.debug("SDA Agent stopped");
			}
			
			onOf = false;
		}
	}
	
	
	
	
	
	public static boolean startShimCalls(){
				
		CallConfig callConfig = new CallConfig(propertiesPath);
	
		CallDaemons callDaemons = new CallDaemons(propertiesPath, callConfig);
		
		callDaemons.start();
		
		return true;
	}
	
	
	
	public static void main(String[] args){
		
		try{
			
			String propertyPath = "/Users/yuluo/Documents/workspace/SDA_Agent/config/config.properties";
			
			ServiceLauncher.start(propertyPath);
			
			if (!ServiceLauncher.startShimCalls()){
				System.out.println("Shutdown");
				shutDown();
			}else{
				System.out.println("[Agent Server working]");
			}
			
		}catch(Exception e){
			log.fatal("Unable to launch service", e);
			shutDown();
		}
	}
}
