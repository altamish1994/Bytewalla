package se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling;

import java.io.Serializable;

/**
 * Created by altamisharif on 11/03/2016.
 */
public class BluetoothBundle implements Serializable {

    String source;
    String dest;
    int bundle_id;
    String payload;
    boolean isAck;

    public boolean isFinal() {
        return isFinal;
    }

    public void setIsFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    boolean isFinal;

    public String getSourceBluetooth() {
        return sourceBluetooth;
    }

    public void setSourceBluetooth(String sourceBluetooth) {
        this.sourceBluetooth   = sourceBluetooth;
        int a=0;
    }

    String sourceBluetooth;

    public BluetoothBundle() {

        isAck=isFinal= false;
    }


    public boolean isAck() {
        return isAck;
    }

    public void setIsAck(boolean isAck) {
        this.isAck = isAck;
    }


    public int getBundle_id() {
        return bundle_id;
    }

    public String getDest() {
        return dest;
    }

    public String getPayload() {
        return payload;
    }

    public String getSource() {
        return source;
    }

    public void setBundle_id(int bundle_id) {
        this.bundle_id = bundle_id;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
