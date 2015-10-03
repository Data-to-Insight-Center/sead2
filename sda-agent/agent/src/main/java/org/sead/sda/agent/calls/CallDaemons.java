package org.sead.sda.agent.calls;

import org.apache.log4j.Logger;

public class CallDaemons {
	
	private Thread[] callDaemons;
	private int numberofcallDaemons;
	private Logger log;
	
	
	public CallDaemons(CallConfig callConfig){
		
		this.numberofcallDaemons = callConfig.getDaemon();
		
		this.callDaemons = new Thread[this.numberofcallDaemons];
		
		SynchronizedReceiverRunnable srr;
		
		try {
			srr = new SynchronizedReceiverRunnable();
			for (int i = 0; i < this.numberofcallDaemons; i++){
				this.callDaemons[i] = new Thread(srr);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public void start() throws IllegalMonitorStateException{
		log = Logger.getLogger(CallDaemons.class);
		
		for (int i = 0; i < this.numberofcallDaemons; i++){
			this.callDaemons[i].start();
			//log.info("Call Daemon [" + i +"] Started");
		
		}
	}

}
