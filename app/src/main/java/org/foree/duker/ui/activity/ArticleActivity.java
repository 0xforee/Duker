package org.foree.duker.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.foree.duker.R;
import org.foree.duker.base.BaseActivity;
import org.foree.duker.rssinfo.RssItem;
import org.foree.duker.utils.LogUtils;

/**
 * Created by foree on 16-7-22.
 */
public class ArticleActivity extends BaseActivity {
    private static final String TAG = ArticleActivity.class.getSimpleName();
    private WebView wbArticleContent;
    private TextView tvArticleTitle, tvArticleFeedName, tvArticleAuthor;
    RssItem rssItem;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rssItem = (RssItem) getIntent().getExtras().getSerializable("entry");

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
           actionBar.setDisplayHomeAsUpEnabled(true);

        setUpWebView();
        fillFab();
        loadBackdrop();
    }

    private void setUpWebView() {
        wbArticleContent = (WebView)findViewById(R.id.wb_article);
        tvArticleTitle = (TextView)findViewById(R.id.tv_article_title);
        tvArticleFeedName = (TextView)findViewById(R.id.tv_article_feed_name);
        tvArticleAuthor = (TextView)findViewById(R.id.tv_article_author);

        tvArticleFeedName.setText(rssItem.getFeedName());
        tvArticleTitle.setText(rssItem.getTitle());

        wbArticleContent.getSettings().setJavaScriptEnabled(true);
        Log.d(TAG, rssItem.getUrl());
        wbArticleContent.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                imgReset();

            }
        });
        LogUtils.log(rssItem.getContent());
        wbArticleContent.getSettings().setDefaultTextEncodingName("UTF-8");
        wbArticleContent.loadDataWithBaseURL(null, rssItem.getContent(),"text/html","utf-8",null);
    }

    private void loadBackdrop() {
        final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        Glide.with(this).load(rssItem.getVisual()).centerCrop().into(imageView);
    }

    private void fillFab() {
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floating_action_button);
        fab.setImageDrawable(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_share).actionBar().color(Color.WHITE));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });
    }

    private void imgReset() {
        // margin:上右下左
        wbArticleContent.loadUrl("javascript:(function(){" +
                "var objs = document.getElementsByTagName('img'); " +
                "for(var i=0;i<objs.length;i++)  " +
                "{"
                + "var img = objs[i];   " +
                "    img.style.maxWidth = 'none';   " +
                "    img.style.height ='auto'; " +
                "    img.style.width = '100vw';" +
                "    img.style.margin= \"10px 0px 10px -8px\" ;" +
                "}" +
                "})()");
    }

}
