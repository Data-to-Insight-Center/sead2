/*
 * Copyright 2015 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author luoyu@indiana.edu
 * @author isuriara@indiana.edu
 */

package org.sead.sda;

import com.jcraft.jsch.*;

import java.util.Properties;
import java.util.Vector;

public class Sftp {

    private Session session;
    private Channel channel;
    private ChannelSftp channelSftp;

    public Sftp() {
        connectSessionAndChannel();
    }


    public boolean setSession() {
        try {
            JSch jsch = new JSch();
            this.session = jsch.getSession(Constants.sdaUser, Constants.sdaHost, 22);
            this.session.setPassword(Constants.sdaPassword);
            return true;
        } catch (JSchException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean connectSessionAndChannel() {

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        if (setSession()) {
            session.setConfig(config);

            try {
                session.connect();

                try {
                    channel = session.openChannel("sftp");
                    channel.connect();
                    channelSftp = (ChannelSftp) channel;
                    return true;
                } catch (Exception e1) {
                    e1.printStackTrace();
                    return false;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }


    public void disConnectSessionAndChannel() {

        if (channelSftp.isConnected()) {
            channelSftp.exit();
            channel.disconnect();
            if (session.isConnected()) {
                session.disconnect();
            }
        }

    }

    public boolean doesFileExist(String filePath) {
        try {
            Vector vals = channelSftp.ls(filePath);
            return vals.size() != 0;
        } catch (SftpException e) {
            // ignore exception as we know this is because the file is not there. just return false
            return false;
        }
    }

    public Stat listFiles(String filePath, Stat stat) {

        String prefix = "|" ;
        for (int i = 0; i < stat.getTab(); i++)
            prefix += " |";
        try {
            Vector vals = channelSftp.ls(filePath);
            int maxTab = stat.getTab();
            double size = 0;
            long noOfFiles = 0;
            for (Object fileref : vals) {
                ChannelSftp.LsEntry file = (ChannelSftp.LsEntry) fileref;
                if (file.getAttrs().isDir()) {
                    if (!file.getFilename().equals(".") && !file.getFilename().equals("..")) {
                        //System.out.println(prefix + "_" + file.getFilename());
                        Stat newStat = new Stat();
                        newStat.setTab(stat.getTab() + 1);
                        listFiles(filePath + "/" + file.getFilename(), newStat);
                        if (newStat.getTab() > maxTab) {
                            maxTab = newStat.getTab();
                        }
                        size += newStat.getSize();
                        noOfFiles += newStat.getNoOfFiles();
                    }
                    continue;
                }
                noOfFiles++;
                //System.out.println(prefix + "_" + file.getFilename() + " | " + file.getAttrs().getSize()*1.0/(1024*1024));
                //System.out.println(prefix + "_" + file.getFilename());
                size += file.getAttrs().getSize() * 1.0 / (1024 * 1024);
            }
            stat.setNoOfFiles(noOfFiles);
            stat.setTab(maxTab);
            stat.setSize(size);
            if (vals.size() != 0) {
                stat.setSuccess(true);
            }
            return stat;
        } catch (SftpException e) {
            // ignore exception as we know this is because the file is not there. just return false
            stat.setSuccess(false);
            return stat;
        }
    }
}
