package org.zarroboogs.smartzpn.ui;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.Toast;

import org.zarroboogs.smartzpn.R;
import org.zarroboogs.smartzpn.loginzpn.ILoginView;
import org.zarroboogs.smartzpn.loginzpn.LoginPresenter;
import org.zarroboogs.smartzpn.loginzpn.LoginPresenterImpl;

/**
 * Created by andforce on 15/7/12.
 */
public class LoginActivity extends AppCompatActivity implements ILoginView{

    private LoginPresenter mLoginPresenter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //透明导航栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        setContentView(R.layout.login_activity);

        mLoginPresenter = new LoginPresenterImpl(this);
        mLoginPresenter.login("wangdiyuan@smartisan.com", "ZPN!@#456", new LoginPresenter.OnLoginListener() {
            @Override
            public void onLogin(String token) {
                Toast.makeText(getApplicationContext(),token,Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public void showProgress() {

    }

    @Override
    public void hideProgress() {

    }

    @Override
    public void switchToMainActivity() {

    }
}
