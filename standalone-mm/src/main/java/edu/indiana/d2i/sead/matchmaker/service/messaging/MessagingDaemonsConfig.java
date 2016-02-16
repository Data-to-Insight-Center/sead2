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
# File:  MessagingDaemonsConfig.java
# Description:  Messaging service daemon configuration.
#
# -----------------------------------------------------------------
# 
*/

package edu.indiana.d2i.sead.matchmaker.service.messaging;

public class MessagingDaemonsConfig{
	private int NumberOfSyncMessagingDaemons;
	private int NumberOfAsyncMessagingDaemons;
	
	public MessagingDaemonsConfig(){
		this.NumberOfSyncMessagingDaemons=1;
		this.NumberOfAsyncMessagingDaemons=1;
	}
	
	public void setNumberOfSyncMessagingDaemons(int numOfQueryDaemons){
		this.NumberOfSyncMessagingDaemons=numOfQueryDaemons;
	}
	public void setNumberOfAsyncMessagingDaemons(int numOfQueryDaemons){
		this.NumberOfAsyncMessagingDaemons=numOfQueryDaemons;
	}
	public int getNumberOfSyncMessagingDaemons(){
		return this.NumberOfSyncMessagingDaemons;
	}
	public int getNumberOfAsyncMessagingDaemons(){
		return this.NumberOfAsyncMessagingDaemons;
	}
}
