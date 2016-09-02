package org.foree.duker.ui.activity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import org.foree.duker.R;
import org.foree.duker.base.BaseActivity;
import org.foree.duker.base.MyApplication;
import org.foree.duker.service.RefreshService;

public class SplashActivity extends BaseActivity{
    private static final String TAG = SplashActivity.class.getSimpleName();
    TextView tv_speak1, tv_speak2, tv_speak3, tv_speak_author, tv_version, tv_app_name;
    private long WAIT_TIME = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //init ApplicationDir
        MyApplication myApplication = new MyApplication();
        myApplication.initApplicationDir();

        //获取窗口，设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // start service
        Intent serviceIntent = new Intent(this, RefreshService.class);
        startService(serviceIntent);

        // set font
        setFont();
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

    private void setFont() {
        //找到两种字体
        AssetManager assetManager = getAssets();
        Typeface tf_mvboli = Typeface.createFromAsset(assetManager, "fonts/mvboli.ttf");
        Typeface tf_hobostd = Typeface.createFromAsset(assetManager, "fonts/HoboStd.otf");

        //找到speak
        tv_speak1 = (TextView) findViewById(R.id.load_tv_speak1);
        tv_speak2 = (TextView) findViewById(R.id.load_tv_speak2);
        tv_speak3 = (TextView) findViewById(R.id.load_tv_speak3);
        tv_speak_author = (TextView) findViewById(R.id.load_tv_speak_author);
        tv_speak1.setTypeface(tf_mvboli);
        tv_speak2.setTypeface(tf_mvboli);
        tv_speak3.setTypeface(tf_mvboli);
        tv_speak_author.setTypeface(tf_mvboli);

        //找到version,并设置字体
        tv_version = (TextView) findViewById(R.id.load_tv_version);
        tv_version.setText(MyApplication.myApplicationVersion);
        tv_version.setTypeface(tf_hobostd);

        //findUnreadEntriesByFeedId app_name, and setTypeface
        tv_app_name = (TextView) findViewById(R.id.load_tv_app_name);
        tv_app_name.setTypeface(tf_hobostd);
    }
}
