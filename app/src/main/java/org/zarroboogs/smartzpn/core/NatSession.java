package org.zarroboogs.smartzpn.core;

public class NatSession {
    public int RemoteIP;
    public short RemotePort;
    public String RemoteHost;
    public int BytesSent;
    public int PacketSent;
    public long LastNanoTime;

    @Override
    public String toString() {
        return "RemoteIP:" + RemoteIP + " RemotePort:" + RemotePort + " RemoteHost:" + RemoteHost;
    }
}
