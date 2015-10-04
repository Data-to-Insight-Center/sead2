package org.sead.sda.agent.service;

import org.apache.log4j.Logger;

import org.sead.sda.agent.calls.CallConfig;
import org.sead.sda.agent.calls.CallDaemons;
import org.sead.sda.agent.engine.PropertiesReader;

public class ServiceLauncher {

    private static boolean onOf = false;
    private static final Logger log = Logger.getLogger(ServiceLauncher.class);


    public static void start() {
        if (!onOf) {
            if (log.isDebugEnabled()) {
                log.debug("SDA Agent started");
            }
            onOf = true;
        }
    }

    public static void shutDown() {
        if (onOf) {
            if (log.isDebugEnabled()) {
                log.debug("SDA Agent stopped");
            }
            onOf = false;
        }
    }

    public static boolean startShimCalls() {
        CallConfig callConfig = new CallConfig();
        CallDaemons callDaemons = new CallDaemons(callConfig);
        callDaemons.start();
        return true;
    }

    public static void main(String[] args) {
        try {
            PropertiesReader.init(args[0]);
            ServiceLauncher.start();
            if (!ServiceLauncher.startShimCalls()) {
                System.out.println("Agent Server shutting down...");
                shutDown();
            } else {
                System.out.println("\nAgent Server started...");
                System.out.println("Polling C3P-R services...\n");
            }
        } catch (Exception e) {
            log.fatal("Unable to launch service", e);
            shutDown();
        }
    }
}
