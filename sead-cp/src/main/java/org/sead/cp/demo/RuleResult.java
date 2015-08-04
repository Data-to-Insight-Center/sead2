/*
 *
 * Copyright 2015 University of Michigan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 *
 * @author myersjd@umich.edu
 */

package org.sead.cp.demo;

public class RuleResult {
	private int score;
	private String message;
	
	private boolean triggered = false;
	
	public int getScore() {
		return score;
	}
	public void setResult(int score, String message) {
		this.score = score;
		this.message = message;
		triggered = true;
	}
	
	public String getMessage() {
		return message;
	}
	
	public boolean wasTriggered() {
		return triggered;
	}


}
