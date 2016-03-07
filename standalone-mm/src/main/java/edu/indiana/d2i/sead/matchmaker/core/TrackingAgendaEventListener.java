package edu.indiana.d2i.sead.matchmaker.core;

import org.drools.core.event.DefaultAgendaEventListener;
import org.kie.api.definition.rule.Rule;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.runtime.rule.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A listener that will track all rule firings in a session.
 *
 * @author Stephen Masters, Isaac Martin
 */
public class TrackingAgendaEventListener extends DefaultAgendaEventListener  {

    private static Logger log = LoggerFactory.getLogger(TrackingAgendaEventListener.class);

    private List<Match> matchList = new ArrayList<Match>();

    @Override
    public void afterMatchFired(AfterMatchFiredEvent event) {
        Rule rule = event.getMatch().getRule();

        String ruleName = rule.getName();
        Map<String, Object> ruleMetaDataMap = rule.getMetaData();

        matchList.add(event.getMatch());
        StringBuilder sb = new StringBuilder("Rule fired: " + ruleName);

        if (ruleMetaDataMap.size() > 0) {
            sb.append("\n  With [" + ruleMetaDataMap.size() + "] meta-data:");
            for (String key : ruleMetaDataMap.keySet()) {
                sb.append("\n    key=" + key + ", value="
                        + ruleMetaDataMap.get(key));
            }
        }

        log.debug(sb.toString());
        System.out.println("\n\n==================== After match " + ruleName + "================\n\n");
    }

    public final List<Match> getMatchList() {
        return matchList;
    }

}