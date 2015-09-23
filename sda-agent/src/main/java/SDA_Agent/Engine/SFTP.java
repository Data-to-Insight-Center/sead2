package Engine;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
	
	private String zipPath;
	private String source;
	private String finalSource;
	
	public SFTP(String propertyPath, String zipPath){
		this.property = new PropertiesReader(propertyPath).getProperties();
		this.zipPath = zipPath;
		
		this.source = this.property.getProperty("SDA");
		this.sftphost = this.property.getProperty("sftp.host");
		this.sftpuser = this.property.getProperty("sftp.user");
		this.sftppass = this.property.getProperty("sftp.pass");
		
		connectSessionAndChannel();
		
		this.finalSource = createDirectory(zipPath)+
				File.separator+zipPath.substring(zipPath.lastIndexOf("/")+1);
		downloadFil(zipPath, this.finalSource);
		
		disConnectSessionAndChannel();
		
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
	
	
	public String createDirectory(String zipPath){
		String dirName = zipPath.substring(zipPath.lastIndexOf("/")+1, zipPath.indexOf(".zip"));
		String sourceDir = this.source + dirName;
				
		
		while(true){
			try{
				channelSftp.stat(sourceDir);
				Date date = new Date();
				DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
				sourceDir += dateFormat.format(date);
				
				int tryNum = 1;
				
				try{
					channelSftp.mkdir(sourceDir);
				}catch(SftpException e1){
					if(tryNum > 3){
						e1.printStackTrace();
						break;
					}else{
						tryNum++;
						continue;
					}					
				}
				break;
				
			}catch(SftpException e){
				
				int tryNum = 1;
				
				try{
					channelSftp.mkdir(sourceDir);
				}catch(SftpException e1){
					if(tryNum > 3){
						e1.printStackTrace();
						break;
					}else{
						tryNum++;
						continue;
					}				
				}
				break;
			}
		}
		
		return sourceDir;
	}
	
	
	
	public void downloadFil(String filePath, String destination){
		
		int tryNum = 1;
		
		while(true){
			try{
				channelSftp.get(filePath, destination);
			}catch(SftpException e){
				if(tryNum > 3){
					e.printStackTrace();
					break;
				}else{
					tryNum++;
					continue;
				}
				
			}
			break;
		}
	}
	
	
	public String getFinalSource(){
		return this.finalSource;
	}
	
	public static void main(String[] args){
		
	}

}
