package org.zarroboogs.smartzpn.core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

import org.zarroboogs.smartzpn.R;
import org.zarroboogs.smartzpn.dns.DnsPacket;
import org.zarroboogs.smartzpn.utils.ProxyUtils;
import org.zarroboogs.smartzpn.tcpip.IPHeader;
import org.zarroboogs.smartzpn.tcpip.TCPHeader;
import org.zarroboogs.smartzpn.tcpip.UDPHeader;
import org.zarroboogs.smartzpn.ui.MenuActivity;

public class LocalVpnService extends VpnService implements Runnable, ProxyConfigLoader.OnProxyConfigLoadListener {

    public static LocalVpnService Instance;
    public static String ConfigUrl;

    private static int ID;
    private static int LOCAL_IP;
    private static ConcurrentHashMap<onStatusChangedListener, Object> sOnStatusChangedListeners = new ConcurrentHashMap<onStatusChangedListener, Object>();

    private Thread mVPNThread;
    private ParcelFileDescriptor mVPNInterface;
    private TcpProxyServer mTCPProxyServer;
    private DnsProxy mDnsProxy;
    private FileOutputStream mVPNOutputStream;

    private byte[] mPacket;
    private IPHeader mIPHeader;
    private TCPHeader mTCPHeader;
    private UDPHeader mUDPHeader;
    private ByteBuffer mDNSBuffer;
    private Handler mHandler;
    private long mSentBytes;
    private long mReceivedBytes;


    private ProxyConfigLoader mProxyCofigLoader;

    public LocalVpnService() {
        ID++;
        mHandler = new Handler();
        mPacket = new byte[20000];
        mIPHeader = new IPHeader(mPacket, 0);
        mTCPHeader = new TCPHeader(mPacket, 20);
        mUDPHeader = new UDPHeader(mPacket, 20);
        mDNSBuffer = ((ByteBuffer) ByteBuffer.wrap(mPacket).position(28)).slice();
        Instance = this;

        System.out.printf("New VPNService(%d)\n", ID);

        mProxyCofigLoader = ProxyConfigLoader.getsInstance();
        mProxyCofigLoader.setOnProxyConfigLoadListener(this);

    }

    @Override
    public void onCreate() {
        System.out.printf("VPNService(%s) created.\n", ID);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String proxyUrl = intent.getStringExtra("PROXY_URL");
        if (!TextUtils.isEmpty(proxyUrl)) {
            mProxyCofigLoader.loadFromUrl(this, proxyUrl);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onProxyConfigLoad() {
        // Start a new session by creating a new thread.
        mVPNThread = new Thread(this, "VPNServiceThread");
        mVPNThread.start();
    }


    public static void addOnStatusChangedListener(onStatusChangedListener listener) {
        if (!sOnStatusChangedListeners.containsKey(listener)) {
            sOnStatusChangedListeners.put(listener, 1);
        }
    }

    public static void removeOnStatusChangedListener(onStatusChangedListener listener) {
        if (sOnStatusChangedListeners.containsKey(listener)) {
            sOnStatusChangedListeners.remove(listener);
        }
    }

    private void onConnectionError() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<onStatusChangedListener, Object> entry : sOnStatusChangedListeners.entrySet()) {
                    entry.getKey().onConnectionError();
                }
            }
        });
    }

    private void onConnectionChanged(final boolean isConn) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<onStatusChangedListener, Object> entry : sOnStatusChangedListeners.entrySet()) {
                    entry.getKey().onConnectionChanged(isConn);
                }
            }
        });
    }

    private void onStatusChanged(final String status, final boolean isRunning) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<onStatusChangedListener, Object> entry : sOnStatusChangedListeners.entrySet()) {
                    entry.getKey().onStatusChanged(status, isRunning);
                }
            }
        });
    }

    public void writeLog(final String format, Object... args) {
        final String logString = String.format(format, args);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<onStatusChangedListener, Object> entry : sOnStatusChangedListeners.entrySet()) {
                    entry.getKey().onLogReceived(logString);
                }
            }
        });
    }

    public void sendUDPPacket(IPHeader ipHeader, UDPHeader udpHeader) {
        try {
            ProxyUtils.ComputeUDPChecksum(ipHeader, udpHeader);
            this.mVPNOutputStream.write(ipHeader.mData, ipHeader.mOffset, ipHeader.getTotalLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void run() {
        try {

            mTCPProxyServer = new TcpProxyServer(0);
            mTCPProxyServer.start();
            writeLog("LocalTcpServer started.");

            mDnsProxy = new DnsProxy();
            mDnsProxy.start();
            writeLog("LocalDnsProxy started.");

//            while (true) {
//                runVPN();
//            }
            runVPN();

        } catch (InterruptedException e) {
            System.out.println(e);
            onConnectionError();
        } catch (Exception e) {
            e.printStackTrace();
            writeLog("Fatal error: %s", e.toString());
            onConnectionError();
        } finally {
            writeLog("SmartProxy terminated.");
            dispose();
        }
    }

    private void runVPN() throws Exception {
        this.mVPNInterface = establishVPN();
        this.mVPNOutputStream = new FileOutputStream(mVPNInterface.getFileDescriptor());
        FileInputStream in = new FileInputStream(mVPNInterface.getFileDescriptor());
        int size = 0;
        while (size != -1) {
            while ((size = in.read(mPacket)) > 0) {
                if (mDnsProxy.Stopped || mTCPProxyServer.Stopped) {
                    in.close();
                    throw new Exception("LocalServer stopped.");
                }
                onIPPacketReceived(mIPHeader, size);
            }
            Thread.sleep(100);
        }
        in.close();
        disconnectVPN();
    }

    void onIPPacketReceived(IPHeader ipHeader, int size) throws IOException {
        switch (ipHeader.getProtocol()) {
            case IPHeader.TCP:
                TCPHeader tcpHeader = mTCPHeader;
                tcpHeader.m_Offset = ipHeader.getHeaderLength();
                if (ipHeader.getSourceIP() == LOCAL_IP) {
                    if (tcpHeader.getSourcePort() == mTCPProxyServer.Port) {// 收到本地TCP服务器数据
                        NatSession session = NatSessionManager.getSession(tcpHeader.getDestinationPort());
                        if (session != null) {
                            ipHeader.setSourceIP(ipHeader.getDestinationIP());
                            tcpHeader.setSourcePort(session.RemotePort);
                            ipHeader.setDestinationIP(LOCAL_IP);

                            ProxyUtils.ComputeTCPChecksum(ipHeader, tcpHeader);
                            mVPNOutputStream.write(ipHeader.mData, ipHeader.mOffset, size);
                            mReceivedBytes += size;
                        } else {
                            System.out.printf("NoSession: %s %s\n", ipHeader.toString(), tcpHeader.toString());
                        }
                    } else {

                        // 添加端口映射
                        int portKey = tcpHeader.getSourcePort();
                        NatSession session = NatSessionManager.getSession(portKey);
                        if (session == null || session.RemoteIP != ipHeader.getDestinationIP() || session.RemotePort != tcpHeader.getDestinationPort()) {
                            session = NatSessionManager.createSession(portKey, ipHeader.getDestinationIP(), tcpHeader.getDestinationPort());
                        }

                        session.LastNanoTime = System.nanoTime();
                        session.PacketSent++;//注意顺序

                        int tcpDataSize = ipHeader.getDataLength() - tcpHeader.getHeaderLength();
                        if (session.PacketSent == 2 && tcpDataSize == 0) {
                            return;//丢弃tcp握手的第二个ACK报文。因为客户端发数据的时候也会带上ACK，这样可以在服务器Accept之前分析出HOST信息。
                        }

                        //分析数据，找到host
                        if (session.BytesSent == 0 && tcpDataSize > 10) {
                            int dataOffset = tcpHeader.m_Offset + tcpHeader.getHeaderLength();
                            String host = HttpHostHeaderParser.parseHost(tcpHeader.m_Data, dataOffset, tcpDataSize);
                            if (host != null) {
                                session.RemoteHost = host;
                            }
                        }

                        // 转发给本地TCP服务器
                        ipHeader.setSourceIP(ipHeader.getDestinationIP());
                        ipHeader.setDestinationIP(LOCAL_IP);
                        tcpHeader.setDestinationPort(mTCPProxyServer.Port);

                        ProxyUtils.ComputeTCPChecksum(ipHeader, tcpHeader);
                        mVPNOutputStream.write(ipHeader.mData, ipHeader.mOffset, size);
                        session.BytesSent += tcpDataSize;//注意顺序
                        mSentBytes += size;
                    }
                }
                break;
            case IPHeader.UDP:
                // 转发DNS数据包：
                UDPHeader udpHeader = mUDPHeader;
                udpHeader.m_Offset = ipHeader.getHeaderLength();
                if (ipHeader.getSourceIP() == LOCAL_IP && udpHeader.getDestinationPort() == 53) {
                    mDNSBuffer.clear();
                    mDNSBuffer.limit(ipHeader.getDataLength() - 8);
                    DnsPacket dnsPacket = DnsPacket.FromBytes(mDNSBuffer);
                    if (dnsPacket != null && dnsPacket.Header.QuestionCount > 0) {
                        mDnsProxy.onDnsRequestReceived(ipHeader, udpHeader, dnsPacket);
                    }
                }
                break;
        }
    }


    private ParcelFileDescriptor establishVPN() throws Exception {
        Builder builder = new Builder();
        builder.setMtu(ProxyConfigLoader.getsInstance().getMTU());
        if (ProxyConfigLoader.IS_DEBUG)
            System.out.printf("setMtu: %d\n", ProxyConfigLoader.getsInstance().getMTU());

        ProxyConfigLoader.IPAddress ipAddress = ProxyConfigLoader.getsInstance().getDefaultLocalIP();
        LOCAL_IP = ProxyUtils.ipStringToInt(ipAddress.Address);
        builder.addAddress(ipAddress.Address, ipAddress.PrefixLength);
        if (ProxyConfigLoader.IS_DEBUG)
            System.out.printf("addAddress: %s/%d\n", ipAddress.Address, ipAddress.PrefixLength);

        for (ProxyConfigLoader.IPAddress dns : ProxyConfigLoader.getsInstance().getDnsServers()) {
            builder.addDnsServer(dns.Address);
            if (ProxyConfigLoader.IS_DEBUG)
                System.out.printf("addDnsServer: %s\n", dns.Address);
        }

        if (ProxyConfigLoader.getsInstance().getRouteList().size() > 0) {
            for (ProxyConfigLoader.IPAddress routeAddress : ProxyConfigLoader.getsInstance().getRouteList()) {
                builder.addRoute(routeAddress.Address, routeAddress.PrefixLength);
                if (ProxyConfigLoader.IS_DEBUG)
                    System.out.printf("addRoute: %s/%d\n", routeAddress.Address, routeAddress.PrefixLength);
            }
            builder.addRoute(ProxyUtils.fakeNetWorkIP(), 16);

            if (ProxyConfigLoader.IS_DEBUG)
                System.out.printf("addRoute for FAKE_NETWORK: %s/%d\n", ProxyUtils.fakeNetWorkIP(), 16);
        } else {
            builder.addRoute("0.0.0.0", 0);
            if (ProxyConfigLoader.IS_DEBUG)
                System.out.printf("addDefaultRoute: 0.0.0.0/0\n");
        }


        Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
        Method method = SystemProperties.getMethod("get", new Class[]{String.class});
        ArrayList<String> servers = new ArrayList<String>();
        for (String name : new String[]{"net.dns1", "net.dns2", "net.dns3", "net.dns4",}) {
            String value = (String) method.invoke(null, name);
            if (value != null && !"".equals(value) && !servers.contains(value)) {
                servers.add(value);
                builder.addRoute(value, 32);
                if (ProxyConfigLoader.IS_DEBUG)
                    System.out.printf("%s=%s\n", name, value);
            }
        }

        Intent intent = new Intent(this, MenuActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setConfigureIntent(pendingIntent);
        builder.setSession(ProxyConfigLoader.getsInstance().getSessionName());

        ParcelFileDescriptor pfdDescriptor = builder.establish();


        onStatusChanged(ProxyConfigLoader.getsInstance().getSessionName() + getString(R.string.vpn_connected_status), true);
        onConnectionChanged(true);
        return pfdDescriptor;
    }

    public void disconnectVPN() {
        try {
            if (mVPNInterface != null) {
                mVPNInterface.close();
                mVPNInterface = null;
            }
        } catch (Exception e) {
            // ignore
        }
        onStatusChanged(ProxyConfigLoader.getsInstance().getSessionName() + getString(R.string.vpn_disconnected_status), false);
        onConnectionChanged(false);
        this.mVPNOutputStream = null;
    }

    private synchronized void dispose() {
        // 断开VPN
        disconnectVPN();

        // 停止TcpServer
        if (mTCPProxyServer != null) {
            mTCPProxyServer.stop();
            mTCPProxyServer = null;
            writeLog("LocalTcpServer stopped.");
        }

        // 停止DNS解析器
        if (mDnsProxy != null) {
            mDnsProxy.stop();
            mDnsProxy = null;
            writeLog("LocalDnsProxy stopped.");
        }

        stopSelf();
        System.exit(0);
    }

    @Override
    public void onDestroy() {
        System.out.printf("VPNService(%s) destoried.\n", ID);
        if (mVPNThread != null) {
            mVPNThread.interrupt();
        }
    }

    public interface onStatusChangedListener {
        public void onStatusChanged(String status, Boolean isRunning);

        public void onLogReceived(String logString);

        public void onConnectionChanged(boolean isConn);

        public void onConnectionError();
    }

}