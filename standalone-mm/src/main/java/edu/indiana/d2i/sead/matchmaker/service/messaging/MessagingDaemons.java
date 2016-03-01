/*
#
# Copyright 2015 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: Matchmaker
# File:  MessagingDaemons.java
# Description:  Messaging service daemons (multiple threads).
#
# -----------------------------------------------------------------
# 
*/

package edu.indiana.d2i.sead.matchmaker.service.messaging;

import edu.indiana.d2i.sead.matchmaker.service.ServiceLauncher;
import edu.indiana.d2i.sead.matchmaker.util.MatchmakerENV;
import org.apache.log4j.Logger;

import java.io.IOException;



/**
 * @author Yuan Luo (yuanluo@indiana.edu)
 */
public class MessagingDaemons  {

	private Thread[] SyncMessagingDeamons;
	private Thread[] AsyncMessagingDeamons;
	private int numOfSyncMessagingDaemons;
	private int numOfAsyncMessagingDaemons;
	
	private Logger log;
	
	public MessagingDaemons(MessagingDaemonsConfig msgdmconf, MessagingConfig msgconf, MatchmakerENV env) throws IOException, ClassNotFoundException{
		this.numOfSyncMessagingDaemons=msgdmconf.getNumberOfSyncMessagingDaemons();
		this.numOfAsyncMessagingDaemons=msgdmconf.getNumberOfAsyncMessagingDaemons();
		this.SyncMessagingDeamons=new Thread[numOfSyncMessagingDaemons];
		this.AsyncMessagingDeamons=new Thread[numOfAsyncMessagingDaemons];
		
		for (int i = 0; i < this.numOfSyncMessagingDaemons; i++) {
			SynchronizedReceiverRunnable qsgrr=new SynchronizedReceiverRunnable(msgconf.clone(), env);
			this.SyncMessagingDeamons[i]= new Thread(qsgrr);
		}
	    
	    for (int i = 0; i < this.numOfAsyncMessagingDaemons; i++) {
	    	AsynchronizedReceiverRunnable asgrr=new AsynchronizedReceiverRunnable(msgconf.clone(), env);
		    this.AsyncMessagingDeamons[i]= new Thread(asgrr);
		}
	    
	    
	}
	
	public void start() throws java.lang.IllegalMonitorStateException{
		log = Logger.getLogger(MessagingDaemons.class);
		for (int i = 0; i < this.numOfSyncMessagingDaemons; i++) {
			log.info("Starting Messaging Sync Deamon ["+i+"] for receiving messages from clients.");
		    this.SyncMessagingDeamons[i].start();
		    log.info("Messaging Sync Deamon ["+i+"] Started.");

		}
		for (int i = 0; i < this.numOfAsyncMessagingDaemons; i++) {
			log.info("Starting Messaging Async Deamon ["+i+"] for receiving messages from clients.");
		    this.AsyncMessagingDeamons[i].start();
		    log.info("Messaging Async Deamon ["+i+"] Started.");

		}
	    
	}
		
	/**
	 * @param args
	 */
	public static void main(String[] args) {
        try {
            
            if (args.length < 1) {
                System.err.println("ERROR: properties file not specified");
                System.err.println("Usage:  ServiceLauncher <propertiesFilePath>");
                throw new Exception("ERROR: properties file not specified");
            }
            String propertiesFilePath = args[0];
            ServiceLauncher.start(propertiesFilePath);
            if(!ServiceLauncher.startMessageReceiverDaemon()){
            	//If MessageReceiverDaemon can't be started, shall we shutdown the whole Server? If yes, add the code here.
            	System.err.println("Unable to launch MessageReceiverDaemon");
            }
           
        } catch (Throwable e) {
        	System.err.println("Unable to launch service");
        }
        System.out.println("Main ends here");

    }

}