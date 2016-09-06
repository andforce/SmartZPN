package org.zarroboogs.smartzpn.tunnel.httpconnect;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.zarroboogs.smartzpn.SmartZpnApplication;
import org.zarroboogs.smartzpn.core.ProxyConfigLoader;
import org.zarroboogs.smartzpn.tunnel.Tunnel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.util.Locale;
import java.util.UUID;

public class HttpConnectTunnel extends Tunnel {

    private boolean m_TunnelEstablished;
    private HttpConnectConfig m_Config;

    public HttpConnectTunnel(HttpConnectConfig config, Selector selector) throws IOException {
        super(config.ServerAddress, selector);
        m_Config = config;
    }

    @Override
    protected void onConnected(ByteBuffer buffer) throws Exception {
        String request = String.format(Locale.ENGLISH, "CONNECT %s:%d HTTP/1.0\r\nProxy-Connection: keep-alive\r\nUser-Agent: %s\r\nX-App-Install-ID: %s\r\n\r\n",
                m_DestAddress.getHostName(),
                m_DestAddress.getPort(),
                ProxyConfigLoader.getsInstance().getUserAgent(),
                getAppInstallID());

        buffer.clear();
        buffer.put(request.getBytes());
        buffer.flip();
        if (this.write(buffer, true)) {//发送连接请求到代理服务器
            this.beginReceive();//开始接收代理服务器响应数据
        }
    }

    String getAppInstallID() {
        SharedPreferences preferences = SmartZpnApplication.getContext().getSharedPreferences("SmartProxy", Context.MODE_PRIVATE);
        String appInstallID = preferences.getString("AppInstallID", null);
        if (appInstallID == null || appInstallID.isEmpty()) {
            appInstallID = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("AppInstallID", appInstallID);
            editor.commit();
        }
        return appInstallID;
    }

    String getVersionName() {
        try {
            PackageManager packageManager = SmartZpnApplication.getContext().getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(SmartZpnApplication.getContext().getPackageName(), 0);
            String version = packInfo.versionName;
            return version;
        } catch (Exception e) {
            return "0.0";
        }
    }

    void trySendPartOfHeader(ByteBuffer buffer) throws Exception {
        int bytesSent = 0;
        if (buffer.remaining() > 10) {
            int pos = buffer.position() + buffer.arrayOffset();
            String firString = new String(buffer.array(), pos, 10).toUpperCase();
            if (firString.startsWith("GET /") || firString.startsWith("POST /")) {
                int limit = buffer.limit();
                buffer.limit(buffer.position() + 10);
                super.write(buffer, false);
                bytesSent = 10 - buffer.remaining();
                buffer.limit(limit);
                if (ProxyConfigLoader.IS_DEBUG)
                    System.out.printf("Send %d bytes(%s) to %s\n", bytesSent, firString, m_DestAddress);
            }
        }
    }


    @Override
    protected void beforeSend(ByteBuffer buffer) throws Exception {
        if (ProxyConfigLoader.getsInstance().isIsolateHttpHostHeader()) {
            trySendPartOfHeader(buffer);//尝试发送请求头的一部分，让请求头的host在第二个包里面发送，从而绕过机房的白名单机制。
        }
    }

    @Override
    protected void afterReceived(ByteBuffer buffer) throws Exception {
        if (!m_TunnelEstablished) {
            //收到代理服务器响应数据
            //分析响应并判断是否连接成功
            String response = new String(buffer.array(), buffer.position(), 12);
            if (response.matches("^HTTP/1.[01] 200$")) {
                buffer.limit(buffer.position());
            } else {
                throw new Exception(String.format("Proxy server responsed an error: %s", response));
            }

            m_TunnelEstablished = true;
            super.onTunnelEstablished();
        }
    }

    @Override
    protected boolean isTunnelEstablished() {
        return m_TunnelEstablished;
    }

    @Override
    protected void onDispose() {
        m_Config = null;
    }


}