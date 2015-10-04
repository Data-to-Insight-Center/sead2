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
