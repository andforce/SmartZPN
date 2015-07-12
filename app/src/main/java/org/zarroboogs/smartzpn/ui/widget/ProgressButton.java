package org.zarroboogs.smartzpn.ui.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;

import com.dd.CircularProgressButton;


/**
 * Created by andforce on 15/7/12.
 */
public class ProgressButton extends CircularProgressButton {

    private MyHandler mHandler;
    private static final int SHOW_PROGRESS = 0x0001;

    public ProgressButton(Context context) {
        super(context);
        mHandler = new MyHandler();
    }

    public ProgressButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHandler = new MyHandler();
    }

    public ProgressButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mHandler = new MyHandler();
    }


    public void setComplete() {
        this.setProgress(100);
    }

    public void setError() {
        this.setProgress(-1);
    }

    public void setIdle() {
        this.setProgress(0);
    }

    public void showProgress(){
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
    }


    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SHOW_PROGRESS:{
                    int newProgress = getProgress() + 5;
                    if (newProgress >= 100){
                        newProgress = 1;
                    }
                    setProgress(newProgress);
                    sendEmptyMessage(SHOW_PROGRESS);
                    break;
                }
            }
        }
    }
}
