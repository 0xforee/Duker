package org.foree.duker.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import org.foree.duker.R;
import org.foree.duker.api.AbsApiFactory;
import org.foree.duker.api.AbsApiHelper;
import org.foree.duker.api.ApiFactory;
import org.foree.duker.api.FeedlyApiHelper;
import org.foree.duker.base.BaseActivity;
import org.foree.duker.base.MyApplication;
import org.foree.duker.net.NetCallback;
import org.foree.duker.rssinfo.RssItem;

import java.io.Serializable;
import java.util.List;

public class SplashActivity extends BaseActivity{
    private static final String TAG = SplashActivity.class.getSimpleName();
    private long mStartTime;
    private long WAIT_TIME = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //init ApplicationDir
        MyApplication myApplication = new MyApplication();
        myApplication.initApplicationDir();

        mStartTime = System.currentTimeMillis();
        //获取窗口，设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        AbsApiFactory absApiFactory = new ApiFactory();
        AbsApiHelper apiHelper = absApiFactory.createApiHelper(FeedlyApiHelper.class);
        apiHelper.getStream("", FeedlyApiHelper.API_GLOBAL_ALL_URL.replace(":userId", FeedlyApiHelper.USER_ID), new NetCallback<List<RssItem>>() {
            @Override
            public void onSuccess(final List<RssItem> data) {
                gotoMainActivity((Serializable) data);

            }

            @Override
            public void onFail(String msg) {
                Toast.makeText(SplashActivity.this, "getGlobalAllError: " + msg, Toast.LENGTH_LONG).show();
                gotoMainActivity(null);
            }
        });

    }

    private void gotoMainActivity(final Serializable data) {
        final long mEndTime = System.currentTimeMillis()-mStartTime;
        final long loadTime = Math.max(WAIT_TIME, mEndTime);
        Log.i(TAG, "loadTime = " + loadTime);
        synchronized (this) {
            try {
                wait(loadTime - mEndTime);
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                intent.putExtra("rssItemList", data);
                startActivity(intent);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finish();
        }

    }

}
