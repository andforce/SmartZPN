package org.zarroboogs.smartzpn.core;

import org.zarroboogs.smartzpn.tunnel.Config;
import org.zarroboogs.smartzpn.tunnel.RawTunnel;
import org.zarroboogs.smartzpn.tunnel.Tunnel;
import org.zarroboogs.smartzpn.tunnel.httpconnect.HttpConnectConfig;
import org.zarroboogs.smartzpn.tunnel.httpconnect.HttpConnectTunnel;
import org.zarroboogs.smartzpn.tunnel.shadowsocks.ShadowsocksConfig;
import org.zarroboogs.smartzpn.tunnel.shadowsocks.ShadowsocksTunnel;

import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;


public class TunnelFactory {

    public static Tunnel wrap(SocketChannel channel, Selector selector) {
        return new RawTunnel(channel, selector);
    }

    public static Tunnel createTunnelByConfig(InetSocketAddress destAddress, Selector selector) throws Exception {
        if (destAddress.isUnresolved()) {
            Config config = ProxyConfigLoader.getsInstance().getDefaultTunnelConfig(destAddress);
            if (config instanceof HttpConnectConfig) {
                return new HttpConnectTunnel((HttpConnectConfig) config, selector);
            } else if (config instanceof ShadowsocksConfig) {
                return new ShadowsocksTunnel((ShadowsocksConfig) config, selector);
            }
            throw new Exception("The config is unknow.");
        } else {
            return new RawTunnel(destAddress, selector);
        }
    }

}
