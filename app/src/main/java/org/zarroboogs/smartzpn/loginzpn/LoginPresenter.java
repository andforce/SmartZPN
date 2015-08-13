package org.zarroboogs.smartzpn.loginzpn;

/**
 * Created by wangdiyuan on 15-8-13.
 */
public interface LoginPresenter {
    public interface OnLoginListener{
        public void onLogin(String token);
    }
    public interface OnRequireProxyListener{
        public void onRequireProxy(String proxySpac);
    }
    public void login(String email, String password, OnLoginListener loginListener);

    public void requireProxy(String token, OnRequireProxyListener requireProxyListener);
}
