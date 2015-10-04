package org.sead.sda.agent.driver;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class FileManager {
	
	public FileManager(){
		
	}
	
	public void removeTempFolder(String dirPath){
		File temp = new File(dirPath);
		try{
			if (temp.exists()){
				FileUtils.deleteDirectory(temp);
			}
		}catch(IOException e){
			System.err.println(e);
		}
	}
	
	
	public void removeTempFile(String downloadPath){
		File temp = new File(downloadPath);
		try{
			if (temp.exists()){
				temp.delete();
			}
		}catch(Exception e){
			System.err.println(e);
		}
	}

	

}
