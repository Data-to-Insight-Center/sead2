package org.sead.nds.repository.util;

public interface StatusReceiver {

	public abstract void sendStatusMessage(String stage, String message);
	
}
