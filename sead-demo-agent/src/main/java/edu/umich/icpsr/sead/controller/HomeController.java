package edu.umich.icpsr.sead.controller;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.umich.icpsr.sead.service.SeadDepositService;

@Controller
public class HomeController {
	@Autowired
	SeadDepositService seadDepositService;

	@RequestMapping("/")
	public String welcome(HttpServletRequest req) {
		return "index";
	}

	@RequestMapping("/sead-cp")
	public String seadRequests(HttpServletRequest req) {
		return "sead-cp";
	}

	@RequestMapping("/invoke")
	public void invokeRequests(HttpServletRequest req, HttpServletResponse res, @RequestParam String researchObjectUrl, @RequestParam(required = false) String ack,
			@RequestParam(required = false) String doi) {
		try {
			PrintWriter writer = res.getWriter();
			writer.println("************** BEGIN *********************");
			writer.flush();
			seadDepositService.processPendingDeposits(researchObjectUrl, writer, ack != null, doi != null);
			writer.println("************** END *********************");
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
