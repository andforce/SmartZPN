package org.zarroboogs.smartzpn.loginzpn;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.zarroboogs.smartzpn.SmartZpnApplication;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by wangdiyuan on 15-8-13.
 */
public class LoginHelper {
    private static final String TAG = "LoginHelper";

    private static final String sContentType = "application/json;charset=UTF-8";
    private AsyncHttpClient mAsyncHttpClient = new AsyncHttpClient();
    private String mLoginTemple = "{\"email\":\"%s\",\"password\":\"%s\"}";
    private AsyncHttpResponseHandler mAsyncHttpResponseHandler;
    private boolean mNeedTryAgain = false;

    public void login(String email, String password, AsyncHttpResponseHandler httpResponseHandler) {
        ArrayList<Header> headerList = builBasicHeaders();
        headerList.add(new BasicHeader("Accept-Encoding", "gzip,deflate"));
        mAsyncHttpResponseHandler = httpResponseHandler;
        mNeedTryAgain = true;
        tryLogin(email, password, headerList);
    }

    private void tryLogin(final String email, final String password, ArrayList<Header> headerList) {
        headerList.add(new BasicHeader("Accept-Encoding", "gzip,deflate"));
        Header[] headers = headerList.toArray(new Header[headerList.size()]);

        String param = String.format(mLoginTemple, email, password);
        try {
            StringEntity entity = new StringEntity(param, "utf-8");
            Log.d(TAG, "login->param:" + param);
            mAsyncHttpClient.post(SmartZpnApplication.getContext(), Constants.Url.LOGIN_URL, headers, entity, sContentType, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    mAsyncHttpResponseHandler.onSuccess(statusCode,headers, responseBody);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    if (mNeedTryAgain){
                        mNeedTryAgain = false;
                        ArrayList<Header> headerList = builBasicHeaders();
                        headerList.add(new BasicHeader("Accept-Encoding", "gzip,deflate,sdch"));
                        tryLogin(email, password,headerList);
                    } else{
                        mAsyncHttpResponseHandler.onFailure(statusCode,headers, responseBody, error);
                    }

                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void requireProxy(String token, AsyncHttpResponseHandler httpResponseHandler) {
        mAsyncHttpClient.get(Constants.Url.requestProxyUrl(token), httpResponseHandler);
    }

    private ArrayList<Header> builBasicHeaders() {
        ArrayList<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Host", "api.zqt.pw"));
        headers.add(new BasicHeader("Connection", "keep-alive"));
        headers.add(new BasicHeader("Accept", "application/json,text/plain,*/*"));
//        headers.add(new BasicHeader("Origin", "https://api.zqt.pw"));
        headers.add(new BasicHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/43.0.2357.130 Chrome/43.0.2357.130 Safari/537.36"));
        headers.add(new BasicHeader("Content-Type", "application/json;charset=UTF-8"));
        headers.add(new BasicHeader("Referer", "https://api.zqt.pw/xdomain"));
//        headers.add(new BasicHeader("Accept-Encoding", "gzip,deflate,sdch"));
//        headers.add(new BasicHeader("Accept-Encoding", "gzip,deflate"));
        headers.add(new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.6,en;q=0.4"));
        return headers;
    }
}
