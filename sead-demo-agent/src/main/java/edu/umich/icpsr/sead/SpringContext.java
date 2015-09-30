package edu.umich.icpsr.sead;

import java.util.Map;

import org.springframework.web.context.WebApplicationContext;

public class SpringContext {
	private static WebApplicationContext context;

	protected static WebApplicationContext getContext() {
		return context;
	}

	protected static void setContext(WebApplicationContext context) {
		SpringContext.context = context;
	}

	public static <T> T getBean(Class<T> type) {
		return SpringContext.context.getBean(type);
	}

	public static <T> T getBean(Class<T> type, String name) {
		return SpringContext.context.getBean(name, type);
	}

	public static <T> Map<String, T> getBeansOfType(Class<T> type) {
		return SpringContext.context.getBeansOfType(type);
	}
}
