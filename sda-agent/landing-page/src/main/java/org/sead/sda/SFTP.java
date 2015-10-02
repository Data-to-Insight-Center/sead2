package org.sead.sda;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SFTP {
	
	private JSch jsch;
	private String sftphost;
	private String sftpuser;
	private String sftppass;
	
	private Session session;
	private Channel channel;
	private ChannelSftp channelSftp;
	private Properties property;
	

	
	public SFTP(String propertyPath){
		this.property = new PropertiesReader(propertyPath).getProperties();
 
		this.sftphost = this.property.getProperty("sftp.host");
		this.sftpuser = this.property.getProperty("sftp.user");
		this.sftppass = this.property.getProperty("sftp.pass");
		connectSessionAndChannel();

		
	}
	
	
	public boolean setSession(){
		try {
			this.jsch = new JSch();
			this.session = this.jsch.getSession(sftpuser, sftphost,22);
			this.session.setPassword(sftppass);
			return true;
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	
	public boolean connectSessionAndChannel(){
		
		Properties config = new Properties();
		config.put("StrictHostKeyChecking","no");
		if (setSession()){
			session.setConfig(config);
			
			try{
				session.connect();	
				
				try{
					channel  = session.openChannel("sftp");
					channel.connect();
					channelSftp = (ChannelSftp) channel;
					return true;
				}catch(Exception e1){
					e1.printStackTrace();
					return false;
				}
				
			}catch(Exception e){
				e.printStackTrace();
				return false;
			}
		}else{
			return false;
		}
	}
	
	

	
	
	public void disConnectSessionAndChannel(){
		
		if (channelSftp.isConnected()){
			channelSftp.exit();	
			channel.disconnect();
			if (session.isConnected()){
				session.disconnect();
			}
		}
        
	}
	
	
	
	
	
	public InputStream downloadFile(String filePath){
		
		int tryNum = 1;

		try {
			InputStream input = channelSftp.get(filePath);
			return input;
		} catch (SftpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	

	
	public static void main(String[] args){
		}

}
