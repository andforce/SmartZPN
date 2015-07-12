package org.zarroboogs.smartzpn.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.yalantis.guillotine.animation.GuillotineAnimation;
import com.yalantis.guillotine.interfaces.GuillotineListener;

import org.zarroboogs.smartzpn.R;
import org.zarroboogs.smartzpn.core.LocalVpnService;
import org.zarroboogs.smartzpn.ui.widget.ProgressButton;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener,LocalVpnService.onStatusChangedListener{

    private Toolbar mToolbar;
    private ProgressButton mConnBtn;
    private GuillotineAnimation mCuillotine;
    private static final int START_VPN_SERVICE_REQUEST_CODE = 1985;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //透明导航栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        setContentView(R.layout.menu_activity);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setTitle(null);
        }

        mConnBtn = (ProgressButton) findViewById(R.id.connectionBtn);

        View guillotineMenu = LayoutInflater.from(this).inflate(R.layout.guillotine, null);
        FrameLayout root = (FrameLayout) findViewById(R.id.root);
        root.addView(guillotineMenu);

        View closeView = findViewById(R.id.content_hamburger);
        View openView = guillotineMenu.findViewById(R.id.guillotine_hamburger);

        mCuillotine = new GuillotineAnimation.GuillotineBuilder(guillotineMenu, openView, closeView)
                .setStartDelay(250)
                .setActionBarViewForAnimation(mToolbar)
                .setClosedOnStart(true)
                .setGuillotineListener(new GuillotineListener() {
                    @Override
                    public void onGuillotineOpened() {
                        mConnBtn.setClickable(false);
                    }

                    @Override
                    public void onGuillotineClosed() {
                        mConnBtn.setClickable(true);
                    }
                })
                .build();

        mConnBtn.setOnClickListener(this);
        LocalVpnService.addOnStatusChangedListener(this);

        if (LocalVpnService.IsRunning){
            mConnBtn.setComplete();
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.connectionBtn){
            if (!LocalVpnService.IsRunning){
                mConnBtn.showProgress();
                mConnBtn.setClickable(false);
                Intent intent = LocalVpnService.prepare(this);
                if (intent == null) {
                    startVPNService();
                } else {
                    startActivityForResult(intent, START_VPN_SERVICE_REQUEST_CODE);
                }
            } else {
                mConnBtn.showProgress();
                LocalVpnService.IsRunning = false;
                LocalVpnService.Instance.disconnectVPN();
                stopService(new Intent(MenuActivity.this, LocalVpnService.class));
                System.runFinalization();
                System.exit(0);

            }
        }
    }

    private void startVPNService() {
        String configUrl = "http://119.254.103.105/pTKII.DGIb8.spac";
        LocalVpnService.ConfigUrl = configUrl;
        startService(new Intent(this, LocalVpnService.class));
    }

    @Override
    public void onStatusChanged(String status, Boolean isRunning) {
        mConnBtn.setClickable(true);
    }

    @Override
    public void onLogReceived(String logString) {

    }

    @Override
    public void onConnectionChanged(boolean isConn) {
        mConnBtn.stopShowProgress();
        if (isConn){
            mConnBtn.setComplete();
        } else {
            mConnBtn.setIdle();
        }

    }

    @Override
    public void onConnectionError() {
        mConnBtn.stopShowProgress();
        mConnBtn.setError();
    }

    @Override
    protected void onDestroy() {
        LocalVpnService.removeOnStatusChangedListener(this);
        super.onDestroy();
    }
}
