package Engine;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesReader {
	
	private Properties properties;
	
	public PropertiesReader(String propertiesPath){
		
		try {
			this.properties = new Properties();
			FileInputStream fileInputStream = new FileInputStream(propertiesPath);
			properties.load(fileInputStream);
			
		}catch(IOException e){
			System.err.println("Error: unable to load properties file" + propertiesPath);
			e.printStackTrace();
			System.exit(-1);
			}
	}
	
	public Properties getProperties(){
		return this.properties;
	}

}
