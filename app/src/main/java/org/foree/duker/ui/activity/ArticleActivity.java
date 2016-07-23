package org.foree.duker.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.foree.duker.R;
import org.foree.duker.base.BaseActivity;

/**
 * Created by foree on 16-7-22.
 */
public class ArticleActivity extends BaseActivity {
    private static final String TAG = ArticleActivity.class.getSimpleName();
    private WebView wb_article;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        wb_article = (WebView)findViewById(R.id.wb_article);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        String Url = getIntent().getStringExtra("entryUrl");
        String Title = getIntent().getStringExtra("entryTitle");

        toolbar.setTitle(Title);
        setSupportActionBar(toolbar);
        wb_article.getSettings().setJavaScriptEnabled(true);

        Log.d(TAG, Url);
        wb_article.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                view.loadUrl(url);
                return true;
            }
        });
        wb_article.loadUrl(Url);
    }
}
