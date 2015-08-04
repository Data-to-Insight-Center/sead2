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
