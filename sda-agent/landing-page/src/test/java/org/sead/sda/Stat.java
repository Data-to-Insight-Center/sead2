package org.sead.sda;

/**
 * Created by charmadu on 4/22/16.
 */
public class Stat {

    private double size;
    private long noOfFiles;
    private int tab;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    boolean success;

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public long getNoOfFiles() {
        return noOfFiles;
    }

    public void setNoOfFiles(long noOfFiles) {
        this.noOfFiles = noOfFiles;
    }

    public int getTab() {
        return tab;
    }

    public void setTab(int tab) {
        this.tab = tab;
    }

    public String toString() {
        return "noOfFiles : " + noOfFiles + ", size : " + size + " MB , depth : "  + tab;
    }
}
