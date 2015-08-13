package org.zarroboogs.smartzpn.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
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
        mPassWordEditText = (MaterialEditText) findViewById(R.id.password);


        mLoginPresenter = new LoginPresenterImpl(this);
    }

    @Override
    public void showProgress() {
        mProgressButton.showProgress();
    }

    @Override
    public void hideProgress() {

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
        String email = mEmailEditText.getText().toString();
        String password = mPassWordEditText.getText().toString();
        Log.d(TAG, "email: " + email + " Pwd: " + password );
        mLoginPresenter.login(email, password);
    }
}
