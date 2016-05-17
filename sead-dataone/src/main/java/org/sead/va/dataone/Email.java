/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.sead.va.dataone;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Email {

	Properties props;
	String from;
	String host;
	String password;
	
	public Email(String type, String fromAddress, String pwd){
		if(type.equalsIgnoreCase("gmail"))
			host = "smtp.gmail.com";

		from = fromAddress;
		password =pwd;
		props = System.getProperties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.user", from);
		props.put("mail.smtp.password", password);
		props.put("mail.smtp.port", "587"); // 587 is the port number of yahoo mail
		props.put("mail.smtp.starttls.enable","true");
		props.put("mail.smtp.auth", "true");
	}
	
	public boolean sendEmail(String toAddress, String subject, String messageStr)
	{
		try
		{
			Session session = Session.getDefaultInstance(props, null);
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
	
			InternetAddress toIntAddress = new InternetAddress(toAddress);
		
			
			message.addRecipient(Message.RecipientType.TO, toIntAddress);
			 
			message.setSubject(subject);
			message.setText(messageStr);
			Transport transport = session.getTransport("smtp");
			transport.connect(host,from,password);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
}
