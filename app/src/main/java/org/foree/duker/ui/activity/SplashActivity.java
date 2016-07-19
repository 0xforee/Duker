package org.foree.duker.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import org.foree.duker.R;
import org.foree.duker.base.BaseActivity;

public class SplashActivity extends BaseActivity {
    private static final String TAG = SplashActivity.class.getSimpleName();
    private long WAIT_TIME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //获取窗口，设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Thread loadThread = new Thread() {
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        wait(WAIT_TIME);
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        finish();
                    }
                }
            }
        };

        loadThread.start();
    }
}
