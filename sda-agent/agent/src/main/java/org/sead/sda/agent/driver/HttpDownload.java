package org.sead.sda.agent.driver;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;

public class HttpDownload {
	
	private HttpURLConnection httpConn;
	private InputStream inputStream;
	private ArrayList<String> errorLinks;
	private String label;
	private String fileUrl;
	
	public HttpDownload(){
		errorLinks = new ArrayList<String>();
	}
	
	
	public void connection(String fileUrl, String userAndpass, String label){
		try{
			this.label = label;
			this.fileUrl = fileUrl;
			URL url = new URL(fileUrl);
			httpConn = (HttpURLConnection) url.openConnection();
			String basicAuth = "Basic " + new String(new Base64().encode(userAndpass.getBytes()));
			httpConn.setRequestProperty ("Authorization", basicAuth);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public boolean validHttp(){
		int responseCode;
		boolean re = false;
		try {
			responseCode = httpConn.getResponseCode();
			if(responseCode == HttpURLConnection.HTTP_OK){
				re = true;
			}else{
				re = false;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			re = false;
			e.printStackTrace();
		}
		
		return re;
	}
	
	
	
	public void downloadFile(String downloadDir){
		if (validHttp()){
			try{
				inputStream = httpConn.getInputStream();
				FileOutputStream outputStream = new FileOutputStream(downloadDir);
				int bytesRead = -1;
				byte[] buffer = new byte[4096];
				while ((bytesRead = inputStream.read(buffer)) != -1){
					outputStream.write(buffer,0,bytesRead);
				}
				
				outputStream.close();
				inputStream.close();
				
				System.out.println("[HTTP FILE: HTTP File downloaded into local server] "+label);

				
			}catch(Exception e){
				e.printStackTrace();
				System.out.println("[HTTP FILE: Invalid HTTP link] "+label);
				errorLinks.add("[Invalid HTTP FILE] "+this.fileUrl);
			}
			
		}else{
			System.err.println("Invalid Link： "+this.fileUrl);
			errorLinks.add("[Invalid HTTP FILE] "+this.fileUrl);
		}
	}
	
	
	public void disconnect(){
		httpConn.disconnect();
	}
	
	
	
	public ArrayList<String> gerErrorLinks(){
		return this.errorLinks;
	}
	
	public static void main(String[] args){
		
	}

}
