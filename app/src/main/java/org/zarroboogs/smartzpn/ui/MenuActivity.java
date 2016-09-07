package org.zarroboogs.smartzpn.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.yalantis.guillotine.animation.GuillotineAnimation;
import com.yalantis.guillotine.interfaces.GuillotineListener;

import org.zarroboogs.smartzpn.R;
import org.zarroboogs.smartzpn.core.LocalVpnService;
import org.zarroboogs.smartzpn.core.SmartVpnService;
import org.zarroboogs.smartzpn.ui.widget.ProgressButton;
import org.zarroboogs.smartzpn.utils.TokenUtils;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener, LocalVpnService.onStatusChangedListener {

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
//        if (LocalVpnService.IsRunning) {
//            mConnBtn.setComplete();
//        }

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

    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.connectionBtn) {

//            String prefix = getPackageName();
//            Intent intent = new Intent(this, SmartVpnService.class)
//                    .putExtra(prefix + ".ADDRESS", "")
//                    .putExtra(prefix + ".PORT", "")
//                    .putExtra(prefix + ".SECRET", "");
//            startService(intent);


            if (true) {
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
                LocalVpnService.Instance.disconnectVPN();
                stopService(new Intent(MenuActivity.this, LocalVpnService.class));
                System.runFinalization();
                System.exit(0);

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == START_VPN_SERVICE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                startVPNService();
            } else {
                mConnBtn.setIdle();
                mConnBtn.setClickable(true);
                onLogReceived("canceled.");
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startVPNService() {
        String configUrl = TokenUtils.getSpec();
        LocalVpnService.ConfigUrl = configUrl;
        Intent intent = new Intent(this, LocalVpnService.class);
        intent.putExtra("PROXY_URL", "https://qypac.net/19kwr8eq");
        startService(intent);
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
        if (isConn) {
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
