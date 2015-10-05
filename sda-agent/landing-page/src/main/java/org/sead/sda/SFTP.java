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

import java.io.InputStream;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SFTP {

    private Session session;
    private Channel channel;
    private ChannelSftp channelSftp;

    public SFTP() {
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

    public InputStream downloadFile(String filePath) {

        int tryNum = 1;

        try {
            InputStream input = channelSftp.get(filePath);
            return input;
        } catch (SftpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

    }

}
