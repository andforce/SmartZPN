package org.zarroboogs.smartzpn.loginzpn;

/**
 * Created by wangdiyuan on 15-8-13.
 */
public class Constants {
    public static class Url {
        private static final String PROXY_ROOT_URL = "http://119.254.103.105/%s.spac";
        private static final String ROOT_URL = "https://api.zqt.pw/api/users/";
        public static final String LOGIN_URL = ROOT_URL + "login";

        public static String buildProxy(String shortId) {
            return String.format(PROXY_ROOT_URL, shortId);
        }

        public static String requestProxyUrl(String token) {
            return ROOT_URL + token + "/proxy";
        }
    }
}
