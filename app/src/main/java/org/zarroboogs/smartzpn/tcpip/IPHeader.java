package org.zarroboogs.smartzpn.tcpip;

import org.zarroboogs.smartzpn.utils.ProxyUtils;

import java.util.Locale;

public class IPHeader {

    public static final short IP = 0x0800;
    public static final byte ICMP = 1;
    public static final byte TCP = 6;
    public static final byte UDP = 17;

    private static final byte offset_ver_ihl = 0; // 0: Version (4 bits) + Internet header length (4// bits)
    private static final byte offset_tos = 1; // 1: Type of service
    private static final short offset_tlen = 2; // 2: Total length
    private static final short offset_identification = 4; // :4 Identification
    private static final short offset_flags_fo = 6; // 6: Flags (3 bits) + Fragment offset (13 bits)
    private static final byte offset_ttl = 8; // 8: Time to live

    public static final byte offset_proto = 9; // 9: Protocol
    public static final short offset_crc = 10; // 10: Header checksum
    public static final int offset_src_ip = 12; // 12: Source address
    public static final int offset_dest_ip = 16; // 16: Destination address
    public static final int offset_op_pad = 20; // 20: Option + Padding

    public byte[] mData;
    public int mOffset;

    public IPHeader(byte[] data, int offset) {
        this.mData = data;
        this.mOffset = offset;
    }

    public void Default() {
        setHeaderLength(20);
        setTos((byte) 0);
        setTotalLength(0);
        setIdentification(0);
        setFlagsAndOffset((short) 0);
        setTTL((byte) 64);
    }

    public int getDataLength() {
        return this.getTotalLength() - this.getHeaderLength();
    }

    public int getHeaderLength() {
        return (mData[mOffset + offset_ver_ihl] & 0x0F) * 4;
    }

    public void setHeaderLength(int value) {
        mData[mOffset + offset_ver_ihl] = (byte) ((4 << 4) | (value / 4));
    }

    public byte getTos() {
        return mData[mOffset + offset_tos];
    }

    public void setTos(byte value) {
        mData[mOffset + offset_tos] = value;
    }

    public int getTotalLength() {
        return ProxyUtils.readShort(mData, mOffset + offset_tlen) & 0xFFFF;
    }

    public void setTotalLength(int value) {
        ProxyUtils.writeShort(mData, mOffset + offset_tlen, (short) value);
    }

    public int getIdentification() {
        return ProxyUtils.readShort(mData, mOffset + offset_identification) & 0xFFFF;
    }

    public void setIdentification(int value) {
        ProxyUtils.writeShort(mData, mOffset + offset_identification, (short) value);
    }

    public short getFlagsAndOffset() {
        return ProxyUtils.readShort(mData, mOffset + offset_flags_fo);
    }

    public void setFlagsAndOffset(short value) {
        ProxyUtils.writeShort(mData, mOffset + offset_flags_fo, value);
    }

    public byte getTTL() {
        return mData[mOffset + offset_ttl];
    }

    public void setTTL(byte value) {
        mData[mOffset + offset_ttl] = value;
    }

    public byte getProtocol() {
        return mData[mOffset + offset_proto];
    }

    public void setProtocol(byte value) {
        mData[mOffset + offset_proto] = value;
    }

    public short getCrc() {
        return ProxyUtils.readShort(mData, mOffset + offset_crc);
    }

    public void setCrc(short value) {
        ProxyUtils.writeShort(mData, mOffset + offset_crc, value);
    }

    public int getSourceIP() {
        return ProxyUtils.readInt(mData, mOffset + offset_src_ip);
    }

    public void setSourceIP(int value) {
        ProxyUtils.writeInt(mData, mOffset + offset_src_ip, value);
    }

    public int getDestinationIP() {
        return ProxyUtils.readInt(mData, mOffset + offset_dest_ip);
    }

    public void setDestinationIP(int value) {
        ProxyUtils.writeInt(mData, mOffset + offset_dest_ip, value);
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "%s->%s Pro=%s,HLen=%d", ProxyUtils.ipIntToString(getSourceIP()), ProxyUtils.ipIntToString(getDestinationIP()), getProtocol(), getHeaderLength());
    }

}
