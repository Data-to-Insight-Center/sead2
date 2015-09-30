package edu.umich.icpsr.sead.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResearchObjectRequest {
	public static class Status {
		public Status() {
		}

		private String date, reporter, message, stage;

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public String getReporter() {
			return reporter;
		}

		public void setReporter(String reporter) {
			this.reporter = reporter;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public String getStage() {
			return stage;
		}

		public void setStage(String stage) {
			this.stage = stage;
		}

	}

	@JsonProperty("Repository")
	private String repository;

	@JsonProperty("Aggregation")
	private HashMap<String, Object> aggregation = new HashMap<String, Object>();

	@JsonProperty("Status")
	private ArrayList<Status> status = new ArrayList<Status>();

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public HashMap<String, Object> getAggregation() {
		return aggregation;
	}

	public void setAggregation(HashMap<String, Object> aggregation) {
		this.aggregation = aggregation;
	}

	public List<Status> getStatus() {
		return status;
	}

	public void setStatus(ArrayList<Status> status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "ResearchObjectRequest [Repository=" + repository + ", aggregation=" + aggregation + ", status=" + status + "]";
	}

}
