package org.zarroboogs.smartzpn.loginzpn;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wangdiyuan on 15-8-13.
 */
public class LoginPresenterImpl implements LoginPresenter {
    private ILoginView mILoginView;
    private LoginHelper mLoginHelper = new LoginHelper();

    public LoginPresenterImpl(ILoginView loginView) {
        this.mILoginView = loginView;
    }

    @Override
    public void login(String email, String password, final OnLoginListener loginListener) {
        mILoginView.showProgress();

        mLoginHelper.login(email, password, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String value = jsonObject.getString("token");
                    if (loginListener != null){
                        loginListener.onLogin(value);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mILoginView.hideProgress();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    @Override
    public void requireProxy(String token, final OnRequireProxyListener requireProxyListener) {
        mILoginView.showProgress();

        mLoginHelper.requireProxy(token, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String value = jsonObject.getString("shortid");
                    if (requireProxyListener != null){
                        requireProxyListener.onRequireProxy(Constants.Url.buildProxy(value));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mILoginView.hideProgress();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }
}
