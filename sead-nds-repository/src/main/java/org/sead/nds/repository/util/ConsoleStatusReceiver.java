package org.sead.nds.repository.util;

public class ConsoleStatusReceiver implements StatusReceiver {

	String reporterID=null;
	public ConsoleStatusReceiver(String reporterID) {
		this.reporterID=reporterID;
	};
	
	
	
	public void sendStatusMessage(String stage, String message) {
	System.out.println("*********************Status Message******************************");
	System.out.println("Reporter: " + getReporter() + ", Stage: " + stage);
	System.out.println("Message Text: " + message);
	System.out.println("*****************************************************************");
	}

	private String getReporter() {
		return reporterID;
	}
}
