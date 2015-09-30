package edu.umich.icpsr.sead;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Config {
	@Value("${build.environment}")
	private String environment;
	@Value("${prod.environment.code}")
	private String prodEnvironmentCode;
	@Value("${app.url}")
	private String appUrl;
	@Value("${sead.messaging.username}")
	private String seadMessagingUserName;
	@Value("${sead.messaging.password}")
	private String seadMessagingPassword;
	@Value("${sead.messaging.hostname}")
	private String seadMessagingHostName;
	@Value("${sead.messaging.hostport}")
	private String seadMessagingPort;
	@Value("${sead.messaging.virtualhost}")
	private String seadMessagingVirtualHost;
	@Value("${sead.messaging.exchangename}")
	private String seadMessagingExchangeName;
	@Value("${sead.messaging.queuename}")
	private String seadMessagingQueueName;
	@Value("${sead.messaging.routingkey}")
	private String seadMessagingRoutingKey;
	@Value("${sead.repository.identifier}")
	private String seadRepositoryIdentifier;

	public String getSeadRepositoryIdentifier() {
		return seadRepositoryIdentifier;
	}

	public void setSeadRepositoryIdentifier(String seadRepositoryIdentifier) {
		this.seadRepositoryIdentifier = seadRepositoryIdentifier;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getAppUrl() {
		return appUrl;
	}

	public void setAppUrl(String appUrl) {
		this.appUrl = appUrl;
	}

	public boolean isLocal() {
		return "local".equalsIgnoreCase(getEnvironment());
	}

	public boolean isDev() {
		return "dev".equalsIgnoreCase(getEnvironment());
	}

	public boolean isTest() {
		return "test".equalsIgnoreCase(getEnvironment());
	}

	public boolean isQA() {
		return "qa".equalsIgnoreCase(getEnvironment());
	}

	public String getSeadMessagingUserName() {
		return seadMessagingUserName;
	}

	public void setSeadMessagingUserName(String seadMessagingUserName) {
		this.seadMessagingUserName = seadMessagingUserName;
	}

	public String getSeadMessagingPassword() {
		return seadMessagingPassword;
	}

	public void setSeadMessagingPassword(String seadMessagingPassword) {
		this.seadMessagingPassword = seadMessagingPassword;
	}

	public String getSeadMessagingHostName() {
		return seadMessagingHostName;
	}

	public void setSeadMessagingHostName(String seadMessagingHostName) {
		this.seadMessagingHostName = seadMessagingHostName;
	}

	public String getSeadMessagingPort() {
		return seadMessagingPort;
	}

	public void setSeadMessagingPort(String seadMessagingPort) {
		this.seadMessagingPort = seadMessagingPort;
	}

	public String getSeadMessagingVirtualHost() {
		return seadMessagingVirtualHost;
	}

	public void setSeadMessagingVirtualHost(String seadMessagingVirtualHost) {
		this.seadMessagingVirtualHost = seadMessagingVirtualHost;
	}

	public String getSeadMessagingExchangeName() {
		return seadMessagingExchangeName;
	}

	public void setSeadMessagingExchangeName(String seadMessagingExchangeName) {
		this.seadMessagingExchangeName = seadMessagingExchangeName;
	}

	public String getSeadMessagingQueueName() {
		return seadMessagingQueueName;
	}

	public void setSeadMessagingQueueName(String seadMessagingQueueName) {
		this.seadMessagingQueueName = seadMessagingQueueName;
	}

	public String getSeadMessagingRoutingKey() {
		return seadMessagingRoutingKey;
	}

	public void setSeadMessagingRoutingKey(String seadMessagingRoutingKey) {
		this.seadMessagingRoutingKey = seadMessagingRoutingKey;
	}

	public String getProdEnvironmentCode() {
		return prodEnvironmentCode;
	}

	public void setProdEnvironmentCode(String prodEnvironmentCode) {
		this.prodEnvironmentCode = prodEnvironmentCode;
	}

	public String seadRabbitMQUri() {
		String seadQueueUri = "rabbitmq://" + getSeadMessagingHostName() + ":" + getSeadMessagingPort() + "/" + getSeadMessagingExchangeName() + "?routingKey=" + getSeadMessagingRoutingKey()
				+ "&username=" + getSeadMessagingUserName() + "&password=" + getSeadMessagingPassword() + "&exchangeType=topic&autoDelete=false&durable=true";
		return seadQueueUri;
	}
}
