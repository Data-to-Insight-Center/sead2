package org.sead.sda.agent.calls;

import org.sead.sda.agent.engine.PropertiesReader;

public class CallConfig {

    private int daemons;

    public CallConfig() {
        this.daemons = Integer.parseInt(PropertiesReader.callDaemons);
    }


    public int getDaemon() {
        return this.daemons;
    }

}
