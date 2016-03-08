/*
#
# Copyright 2015 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: MatchMaker Service
# File:  MatchMaker.java
# Description:  Fire matchmaking rules.
#
# -----------------------------------------------------------------
# 
*/
package edu.indiana.d2i.sead.matchmaker.core;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;


public class MatchMaker {

    public void basicGo(PrintStream out, File[] ruleFiles, String[] classNames, MatchMakingList initList, Object[] repositories, Object person, Object researchObject) throws ClassNotFoundException, JsonParseException, JsonMappingException, IOException, InstantiationException, IllegalAccessException {
        KieServices ks = KieServices.Factory.get();

        /*KieRepository kr = ks.getRepository();
        KieModule kModule=null;
        for(File rulefile : ruleFiles){
        	kModule = kr.addKieModule(ks.getResources().newFileSystemResource(rulefile));
        }
        KieContainer kContainer = ks.newKieContainer(kModule.getReleaseId());*/

        /*KieContainer kContainer = ks.getKieClasspathContainer();

        KieSession kSession = kContainer.newKieSession("ksession-rules");*/


        KieRepository kr = ks.getRepository();
        KieFileSystem kfs = ks.newKieFileSystem();

        kfs.write(ResourceFactory.newClassPathResource("rules/ruleset1.drl", this.getClass()));

        KieBuilder kb = ks.newKieBuilder(kfs);

        kb.buildAll(); // kieModule is automatically deployed to KieRepository if successfully built.
        if (kb.getResults().hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n" + kb.getResults().toString());
        }

        KieContainer kContainer = ks.newKieContainer(kr.getDefaultReleaseId());

        KieSession kSession = kContainer.newKieSession();
        TrackingAgendaEventListener trackingAgendaEventListener = new TrackingAgendaEventListener();
        //kSession.addEventListener(trackingAgendaEventListener);

        for(Object repo:repositories){
            kSession.insert(repo);
        }
        kSession.insert(person);
        kSession.insert(researchObject);
        kSession.insert(initList);
        for(String className : classNames){
           kSession.insert(Class.forName(className).newInstance());
        }
        System.out.println("Fire All Rules...");
        kSession.fireAllRules();
        //initList.printCandidateList();

        /*Map rules = new HashMap<String , String>();
        for(Match match : trackingAgendaEventListener.getMatchList()){
            rules.put(match.getRule().getName(), "rule");
        }*/

        kSession.dispose();

/*
        initList.addUnmatchedRules(rules.keySet());
*/
    }

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, JsonParseException, JsonMappingException, IOException, InstantiationException {
    }
}
