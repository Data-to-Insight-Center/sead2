package edu.umich.icpsr.sead;

import javax.servlet.ServletException;

import org.springframework.web.servlet.DispatcherServlet;

public class AppDispatcherServlet extends DispatcherServlet {
	private static final long serialVersionUID = -2450331553081180248L;

	@Override
	protected void initFrameworkServlet() throws ServletException {
		super.initFrameworkServlet();
		SpringContext.setContext(getWebApplicationContext());
	}

}
