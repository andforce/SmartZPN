package org.zarroboogs.smartzpn.loginzpn;

import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import org.zarroboogs.smartzpn.utis.TokenUtils;

/**
 * Created by wangdiyuan on 15-8-13.
 */
public class LoginPresenterImpl implements LoginPresenter {
    private static final String TAG = "LoginPresenterImpl";
    private ILoginView mILoginView;
    private LoginHelper mLoginHelper = new LoginHelper();

    public LoginPresenterImpl(ILoginView loginView) {
        this.mILoginView = loginView;
    }

    @Override
    public void login(String email, String password) {
        mILoginView.showProgress();

        mLoginHelper.login(email, password, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                Log.d(TAG,"LoginPresenterImpl->login->onSuccess "+ result);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String value = jsonObject.getString("token");
                    TokenUtils.setToken(value);

                    requireProxy(value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mILoginView.hideProgress();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String result = new String(responseBody);
                Log.d(TAG , "LoginPresenterImpl->login->onFailure "+ result + "  statusCode:" + statusCode);
            }
        });
    }

    private void requireProxy(String token) {
        mILoginView.showProgress();

        mLoginHelper.requireProxy(token, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String value = jsonObject.getString("shortid");
                    TokenUtils.setSpec(Constants.Url.buildProxy(value));
                    mILoginView.switchToMainActivity();

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
