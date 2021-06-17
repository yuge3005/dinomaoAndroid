package com.dinomao.livearcade;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.graphics.Bitmap;
import android.content.res.Resources;

public class MainActivity extends AppCompatActivity {

    private WebViewClient webViewClient = new WebViewClientForMain();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String url = "http://10.0.2.2:4200";
        WebView webView = (WebView) findViewById(R.id.web_view);

        WebView.setWebContentsDebuggingEnabled(true);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setDomStorageEnabled( true );
//        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        // settings.setSupportZoom(true);
        // settings.setBuiltInZoomControls(true);
        webView.setWebViewClient(webViewClient);
        webView.setWebChromeClient(new WebChromeClient());
//        webView.addJavascriptInterface();
        webView.addJavascriptInterface( new AndroidLogger(), "androidLogger");

//        WebStorage.getInstance().deleteAllData();

        WebViewClientForMain.androidId = Settings.System.getString( getBaseContext().getContentResolver(), Settings.System.ANDROID_ID );
        WebViewClientForMain.mainUrl = url;
        webView.loadUrl( WebViewClientForMain.getMainUrl() );
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
}