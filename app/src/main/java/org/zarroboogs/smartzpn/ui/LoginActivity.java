package org.zarroboogs.smartzpn.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import org.zarroboogs.smartzpn.R;
import org.zarroboogs.smartzpn.loginzpn.ILoginView;
import org.zarroboogs.smartzpn.loginzpn.LoginPresenter;
import org.zarroboogs.smartzpn.loginzpn.LoginPresenterImpl;
import org.zarroboogs.smartzpn.ui.widget.ProgressButton;
import org.zarroboogs.smartzpn.utis.DeviceUtils;

/**
 * Created by andforce on 15/7/12.
 */
public class LoginActivity extends AppCompatActivity implements ILoginView, View.OnClickListener{

    private static String TAG = "LoginActivity";
    private LoginPresenter mLoginPresenter;
    private ProgressButton mProgressButton;
    private MaterialEditText mEmailEditText;
    private MaterialEditText mPassWordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //透明导航栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        setContentView(R.layout.login_activity);
        mProgressButton = (ProgressButton) findViewById(R.id.connectionBtn);
        mProgressButton.setOnClickListener(this);

        mEmailEditText = (MaterialEditText) findViewById(R.id.username);
        mEmailEditText.setOnClickListener(this);
        mPassWordEditText = (MaterialEditText) findViewById(R.id.password);
        mPassWordEditText.setOnClickListener(this);


        mLoginPresenter = new LoginPresenterImpl(this);
    }

    @Override
    public void showProgress() {
        mProgressButton.showProgress();
    }

    @Override
    public void hideProgress() {
        mProgressButton.stopShowProgress();
    }

    @Override
    public void showFailed() {
        mProgressButton.setError();
    }

    @Override
    public void switchToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.connectionBtn){
            if (DeviceUtils.checkNet(getApplicationContext())){
                String email = mEmailEditText.getText().toString();
                if (TextUtils.isEmpty(email)){
                    Toast.makeText(getApplicationContext(), R.string.login_email_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                String password = mPassWordEditText.getText().toString();
                if (TextUtils.isEmpty(password)){
                    Toast.makeText(getApplicationContext(), R.string.login_pwd_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d(TAG, "email: " + email + " Pwd: " + password );
                mLoginPresenter.login(email, password);
            } else{
                Toast.makeText(getApplicationContext(), R.string.login_network_error, Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.username || id == R.id.password){
            mProgressButton.setIdle();
        }

    }
}
