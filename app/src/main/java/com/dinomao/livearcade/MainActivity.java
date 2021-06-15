package com.dinomao.livearcade;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.graphics.Bitmap;
import android.content.res.Resources;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String url = "https://staging.dinomao.com/";
        WebView webView = (WebView) findViewById(R.id.web_view);
        webView.loadUrl(url);

        WebView.setWebContentsDebuggingEnabled(true);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        // settings.setSupportZoom(true);
        // settings.setBuiltInZoomControls(true);

        // webView.setWebViewClient(webViewClient);
        Settings.System.getString( context.getContentResolver(), Settings.System.ANDROID_ID );
    }

    @Override
    protected void onPause(){
        super.onPause();
        WebView webView = (WebView) findViewById(R.id.web_view);
        webView.loadUrl("javascript:androidPause()");
    }

    @Override
    protected void onResume(){
        super.onResume();
        WebView webView = (WebView) findViewById(R.id.web_view);
        webView.loadUrl("javascript:androidResume()");
    }

    @Override
    public Resources getResources() {//还原字体大小
        Resources res = super.getResources();
        Configuration configuration = res.getConfiguration();
        if (configuration.fontScale != 1.0f) {
            configuration.fontScale = 1.0f;
            res.updateConfiguration(configuration, res.getDisplayMetrics());
        }
        return res;
    }  

    private WebViewClient webViewClient = new WebViewClient(){
        @Override
        public void onPageFinished(WebView view, String url) {//ҳ��������
            // progressBar.setVisibility(view.GONE);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {//ҳ�濪ʼ����
            // progressBar.setVisibility(view.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
            // return super.shouldOverrideUrlLoading(view, url);
        }

    };
}