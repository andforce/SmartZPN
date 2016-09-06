package org.zarroboogs.smartzpn.core;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.zarroboogs.smartzpn.tcpip.CommonMethods;
import org.zarroboogs.smartzpn.tunnel.Config;
import org.zarroboogs.smartzpn.tunnel.httpconnect.HttpConnectConfig;
import org.zarroboogs.smartzpn.tunnel.shadowsocks.ShadowsocksConfig;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ProxyConfigLoader {
    private static final ProxyConfigLoader sInstance = new ProxyConfigLoader();
    public final static boolean IS_DEBUG = true;
    public final static int FAKE_NETWORK_MASK = CommonMethods.ipStringToInt("255.255.0.0");
    public final static int FAKE_NETWORK_IP = CommonMethods.ipStringToInt("10.231.0.0");

    private ArrayList<IPAddress> mIPList;
    private ArrayList<IPAddress> mDnsServers;
    private ArrayList<IPAddress> mRouteList;
    private ArrayList<Config> mProxyConfigList;
    private HashMap<String, Boolean> mDomainMap;

    private int m_dns_ttl;
    private String m_welcome_info;
    private String m_session_name;
    private String m_user_agent;
    private boolean m_outside_china_use_proxy = true;
    private boolean m_isolate_http_host_header = true;
    private int m_mtu;

    public static ProxyConfigLoader getsInstance() {
        return sInstance;
    }

    public class IPAddress {
        public final String Address;
        public final int PrefixLength;

        public IPAddress(String address, int prefixLength) {
            this.Address = address;
            this.PrefixLength = prefixLength;
        }

        public IPAddress(String ipAddresString) {
            String[] arrStrings = ipAddresString.split("/");
            String address = arrStrings[0];
            int prefixLength = 32;
            if (arrStrings.length > 1) {
                prefixLength = Integer.parseInt(arrStrings[1]);
            }
            this.Address = address;
            this.PrefixLength = prefixLength;
        }

        @Override
        public String toString() {
            return String.format(Locale.ENGLISH, "%s/%d", Address, PrefixLength);
        }

        @Override
        public boolean equals(Object o) {
            return o != null && this.toString().equals(o.toString());
        }
    }

    public ProxyConfigLoader() {
        mIPList = new ArrayList<IPAddress>();
        mDnsServers = new ArrayList<IPAddress>();
        mRouteList = new ArrayList<IPAddress>();
        mProxyConfigList = new ArrayList<Config>();
        mDomainMap = new HashMap<String, Boolean>();

        Timer m_Timer = new Timer();
        m_Timer.schedule(timerTask, 120000, 120000);//每两分钟刷新一次。
    }

    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            refreshProxyServer();//定时更新dns缓存
        }

        //定时更新dns缓存
        void refreshProxyServer() {
            try {
                for (int i = 0; i < mProxyConfigList.size(); i++) {
                    try {
                        Config config = mProxyConfigList.get(0);
                        InetAddress address = InetAddress.getByName(config.ServerAddress.getHostName());
                        if (address != null && !address.equals(config.ServerAddress.getAddress())) {
                            config.ServerAddress = new InetSocketAddress(address, config.ServerAddress.getPort());
                        }
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception ignored) {

            }
        }
    };


    public static boolean isFakeIP(int ip) {
        return (ip & ProxyConfigLoader.FAKE_NETWORK_MASK) == ProxyConfigLoader.FAKE_NETWORK_IP;
    }

    public Config getDefaultProxy() {
        if (mProxyConfigList.size() > 0) {
            return mProxyConfigList.get(0);
        } else {
            return null;
        }
    }

    public Config getDefaultTunnelConfig(InetSocketAddress destAddress) {
        return getDefaultProxy();
    }

    public IPAddress getDefaultLocalIP() {
        if (mIPList.size() > 0) {
            return mIPList.get(0);
        } else {
            return new IPAddress("10.8.0.2", 32);
        }
    }

    public ArrayList<IPAddress> getDnsServers() {
        return mDnsServers;
    }

    public ArrayList<IPAddress> getRouteList() {
        return mRouteList;
    }

    public int getDnsTTL() {
        if (m_dns_ttl < 30) {
            m_dns_ttl = 30;
        }
        return m_dns_ttl;
    }

    public String getWelcomeInfo() {
        return m_welcome_info;
    }

    public String getSessionName() {
        if (m_session_name == null) {
            m_session_name = getDefaultProxy().ServerAddress.getHostName();
        }
        return m_session_name;
    }

    public String getUserAgent() {
        if (m_user_agent == null || m_user_agent.isEmpty()) {
            m_user_agent = System.getProperty("http.agent");
        }
        return m_user_agent;
    }

    public int getMTU() {
        if (m_mtu > 1400 && m_mtu <= 20000) {
            return m_mtu;
        } else {
            return 20000;
        }
    }

    private Boolean getDomainState(String domain) {
        domain = domain.toLowerCase();
        while (domain.length() > 0) {
            Boolean stateBoolean = mDomainMap.get(domain);
            if (stateBoolean != null) {
                return stateBoolean;
            } else {
                int start = domain.indexOf('.') + 1;
                if (start > 0 && start < domain.length()) {
                    domain = domain.substring(start);
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    public boolean needProxy(String host, int ip) {
        if (host != null) {
            Boolean stateBoolean = getDomainState(host);
            if (stateBoolean != null) {
                return stateBoolean;
            }
        }

        if (isFakeIP(ip))
            return true;

        if (m_outside_china_use_proxy && ip != 0) {
            return !ChinaIpMaskManager.isIPInChina(ip);
        }
        return false;
    }

    public boolean isIsolateHttpHostHeader() {
        return m_isolate_http_host_header;
    }

    private String[] downloadConfig(String url) throws Exception {
        try {
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder().url(url).get().build();
            Call call = okHttpClient.newCall(request);
            Response response = call.execute();
            return response.body().string().split("\\n");
        } catch (Exception e) {
            throw new Exception(String.format("Download config file from %s failed.", url));
        }
    }

    private String[] readConfigFromFile(String path) throws Exception {
        StringBuilder sBuilder = new StringBuilder();
        FileInputStream inputStream = null;
        try {
            byte[] buffer = new byte[8192];
            int count = 0;
            inputStream = new FileInputStream(path);
            while ((count = inputStream.read(buffer)) > 0) {
                sBuilder.append(new String(buffer, 0, count, "UTF-8"));
            }
            return sBuilder.toString().split("\\n");
        } catch (Exception e) {
            throw new Exception(String.format("Can't read config file: %s", path));
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public void loadFromUrl(String url) throws Exception {
        String[] lines = null;
        if (url.charAt(0) == '/') {
            lines = readConfigFromFile(url);
        } else {
            lines = downloadConfig(url);
        }

        mIPList.clear();
        mDnsServers.clear();
        mRouteList.clear();
        mProxyConfigList.clear();
        mDomainMap.clear();

        int lineNumber = 0;
        for (String line : lines) {
            lineNumber++;
            String[] items = line.split("\\s+");
            if (items.length < 2) {
                continue;
            }

            String tagString = items[0].toLowerCase(Locale.ENGLISH).trim();
            try {
                if (!tagString.startsWith("#")) {
                    if (ProxyConfigLoader.IS_DEBUG)
                        System.out.println(line);

                    switch (tagString) {
                        case "ip":
                            addIPAddressToList(items, 1, mIPList);
                            break;
                        case "dns":
                            addIPAddressToList(items, 1, mDnsServers);
                            break;
                        case "route":
                            addIPAddressToList(items, 1, mRouteList);
                            break;
                        case "proxy":
                            addProxyToList(items, 1);
                            break;
                        case "direct_domain":
                            addDomainToHashMap(items, 1, false);
                            break;
                        case "proxy_domain":
                            addDomainToHashMap(items, 1, true);
                            break;
                        case "dns_ttl":
                            m_dns_ttl = Integer.parseInt(items[1]);
                            break;
                        case "welcome_info":
                            m_welcome_info = line.substring(line.indexOf(" ")).trim();
                            break;
                        case "session_name":
                            m_session_name = items[1];
                            break;
                        case "user_agent":
                            m_user_agent = line.substring(line.indexOf(" ")).trim();
                            break;
                        case "outside_china_use_proxy":
                            m_outside_china_use_proxy = convertToBool(items[1]);
                            break;
                        case "isolate_http_host_header":
                            m_isolate_http_host_header = convertToBool(items[1]);
                            break;
                        case "mtu":
                            m_mtu = Integer.parseInt(items[1]);
                            break;
                    }
                }
            } catch (Exception e) {
                throw new Exception(String.format(Locale.ENGLISH, "SmartProxy config file parse error: line:%d, tag:%s, error:%s", lineNumber, tagString, e));
            }

        }

        //查找默认代理。
        if (mProxyConfigList.size() == 0) {
            tryAddProxy(lines);
        }
    }

    private void tryAddProxy(String[] lines) {
        for (String line : lines) {
            Pattern p = Pattern.compile("proxy\\s+([^:]+):(\\d+)", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(line);
            while (m.find()) {
                HttpConnectConfig config = new HttpConnectConfig();
                config.ServerAddress = new InetSocketAddress(m.group(1), Integer.parseInt(m.group(2)));
                if (!mProxyConfigList.contains(config)) {
                    mProxyConfigList.add(config);
                    mDomainMap.put(config.ServerAddress.getHostName(), false);
                }
            }
        }
    }

    private void addProxyToList(String[] items, int offset) throws Exception {
        for (int i = offset; i < items.length; i++) {
            String proxyString = items[i].trim();
            Config config = null;
            if (proxyString.startsWith("ss://")) {
                config = ShadowsocksConfig.parse(proxyString);
            } else {
                if (!proxyString.toLowerCase().startsWith("http://")) {
                    proxyString = "http://" + proxyString;
                }
                config = HttpConnectConfig.parse(proxyString);
            }
            if (!mProxyConfigList.contains(config)) {
                mProxyConfigList.add(config);
                mDomainMap.put(config.ServerAddress.getHostName(), false);
            }
        }
    }

    private void addDomainToHashMap(String[] items, int offset, Boolean state) {
        for (int i = offset; i < items.length; i++) {
            String domainString = items[i].toLowerCase().trim();
            if (domainString.charAt(0) == '.') {
                domainString = domainString.substring(1);
            }
            mDomainMap.put(domainString, state);
        }
    }

    private boolean convertToBool(String valueString) {
        if (valueString == null || valueString.isEmpty())
            return false;
        valueString = valueString.toLowerCase(Locale.ENGLISH).trim();
        if (valueString.equals("on") || valueString.equals("1") || valueString.equals("true") || valueString.equals("yes")) {
            return true;
        } else {
            return false;
        }
    }


    private void addIPAddressToList(String[] items, int offset, ArrayList<IPAddress> list) {
        for (int i = offset; i < items.length; i++) {
            String item = items[i].trim().toLowerCase();
            if (item.startsWith("#")) {
                break;
            } else {
                IPAddress ip = new IPAddress(item);
                if (!list.contains(ip)) {
                    list.add(ip);
                }
            }
        }
    }

}
