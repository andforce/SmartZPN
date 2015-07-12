package org.zarroboogs.smartzpn.ui;

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

public class MenuActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private GuillotineAnimation mCuillotine;
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
                        Toast.makeText(getApplicationContext(),"Open", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onGuillotineClosed() {
                        Toast.makeText(getApplicationContext(),"Close", Toast.LENGTH_LONG).show();
                    }
                })
                .build();

    }


}
