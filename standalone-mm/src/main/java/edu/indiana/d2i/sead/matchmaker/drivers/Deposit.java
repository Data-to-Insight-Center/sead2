package edu.indiana.d2i.sead.matchmaker.drivers;

import com.fasterxml.jackson.databind.JsonNode;
import edu.indiana.d2i.sead.matchmaker.util.MatchmakerENV;
import org.apache.log4j.Logger;

public class Deposit extends MetaDriver {
	private Logger log;
	private JsonNode request;
	
	public Deposit(MatchmakerENV env, JsonNode request, String responseID){
		super(env, request.get("message").toString(), responseID);
		this.request = request;
		log = Logger.getLogger(Deposit.class);
		
	}
	
	public String exec() {
		/*Set<String> repositories= candidateList.getCandidateList().keySet();
		String repoStrings = "";
		boolean depositFlag = true;
		for (String repo : repositories ){
			boolean result = false;
			try{
				String MessageConfigPath=this.getENV().getRepoPropertiesPath();
				//long startTime = System.currentTimeMillis();
				MessagingConfig msgconf=new MessagingConfig(MessageConfigPath);
				msgconf.setBaseRoutingKey(getBaseRoutingKeyForRepo(repo));
				AsynchronizedClient asynchronizedClient=new AsynchronizedClient(msgconf);
				result=asynchronizedClient.request(this.getRequest().toString());
				asynchronizedClient.closeConnection();
				
				log.info("Message to be sent to Repo: "+this.getMessage());
				log.info("Repo Config Path: "+MessageConfigPath);
				log.info("Get Repo Async Exchange: "+msgconf.getAsyncRequestExchangeName());
				log.info("Get Repo Async RoutingKey: "+msgconf.getAsyncRequestRoutingKey());
				
				
				depositFlag = depositFlag&&result;
			}catch(Exception e){
				e.printStackTrace();
				depositFlag = depositFlag&&false;
				
			}
			repoStrings = repoStrings + " " +repo +"("+result+")";
			
		}
		log.info("{\n\"responseID\":\""+responseID+"\",\n\"sucess\":"+depositFlag+",\n\"response\": \"Deposit requests sent to "+repoStrings+"\"\n}");
		
		return "{\n\"responseID\":\""+responseID+"\",\n\"sucess\":"+depositFlag+",\n\"response\": \"Deposit requests sent to "+repoStrings+"\"\n}";
	*/

        return "";
    }
	
	public String getBaseRoutingKeyForRepo(String repoName){
		//TODO: right now the routingkey to a repo is the same as the repo name. Need to make this configurable by add a json file for mapping repo names to routingkeys.
		return repoName;
	}

	public JsonNode getRequest(){
		return this.request;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
