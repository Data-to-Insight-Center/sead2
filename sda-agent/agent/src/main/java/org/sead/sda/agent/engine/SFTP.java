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

package org.sead.sda.agent.engine;

import java.io.File;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SFTP {

    private Session session;
    private Channel channel;
    private ChannelSftp channelSftp;

    public SFTP(String zipPath) throws Exception {
        connectSessionAndChannel();

        String finalSource = createDirectory(zipPath) +
                File.separator + zipPath.substring(zipPath.lastIndexOf("/") + 1);

        depositFile(zipPath, finalSource);

        disconnectSessionAndChannel();
    }

    public void setSession() throws Exception {
        JSch jsch = new JSch();
        this.session = jsch.getSession(PropertiesReader.sdaUser, PropertiesReader.sdaHost, 22);
        this.session.setPassword(PropertiesReader.sdaPassword);
    }

    public void connectSessionAndChannel() throws Exception {
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        setSession();
        session.setConfig(config);
        session.connect();
        channel = session.openChannel("sftp");
        channel.connect();
        channelSftp = (ChannelSftp) channel;
    }

    public void disconnectSessionAndChannel() {
        if (channelSftp.isConnected()) {
            channelSftp.exit();
            channel.disconnect();
            if (session.isConnected()) {
                session.disconnect();
            }
        }
    }

    public String createDirectory(String zipPath) throws Exception {
        String dirName = zipPath.substring(zipPath.lastIndexOf("/") + 1, zipPath.indexOf(".tar"));
        String sourceDir = PropertiesReader.sdaPath + dirName;
        try {
            channelSftp.stat(sourceDir);
        } catch (SftpException dirNonExistant) {
            int tryNum = 1;
            while (true) {
                try {
                    channelSftp.mkdir(sourceDir);
                } catch (SftpException e) {
                    if (tryNum > 3) {
                        throw new Exception("Couldn't create dir in SDA in 3 tries", e);
                    } else {
                        tryNum++;
                        continue;
                    }
                }
                break;
            }
        }
        return sourceDir;
    }

    public void depositFile(String filePath, String destination) throws Exception {
        int tryNum = 1;
        while (true) {
            try {
                channelSftp.put(filePath, destination);
            } catch (SftpException e) {
                if (tryNum > 3) {
                    throw new Exception("Couldn't deposit file in 3 tries", e);
                } else {
                    tryNum++;
                    continue;
                }
            }
            break;
        }
    }

}
