package org.zarroboogs.smartzpn.loginzpn;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.zarroboogs.smartzpn.SmartZpnApplication;

import java.io.UnsupportedEncodingException;

/**
 * Created by wangdiyuan on 15-8-13.
 */
public class LoginHelper {
    private static final String sContentType = "application/json;charset=UTF-8";
    private AsyncHttpClient mAsyncHttpClient = new AsyncHttpClient();
    private String mLoginTemple = "{\"email\":\"%s\",\"password\":\"%s\"}";

    public void login(String email, String password, AsyncHttpResponseHandler httpResponseHandler) {
        String param = String.format(mLoginTemple, email, password);
        try {
            StringEntity entity = new StringEntity(param, "utf-8");
            mAsyncHttpClient.post(SmartZpnApplication.getContext(), Constants.Url.LOGIN_URL, entity, sContentType, httpResponseHandler);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void requireProxy(String token, AsyncHttpResponseHandler httpResponseHandler){
        mAsyncHttpClient.get(Constants.Url.requestProxyUrl(token), httpResponseHandler);
    }
}
