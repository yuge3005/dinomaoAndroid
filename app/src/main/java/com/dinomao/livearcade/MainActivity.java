package com.dinomao.livearcade;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.graphics.Bitmap;
import android.content.res.Resources;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {

    private WebViewClient webViewClient = new WebViewClientForMain();

    private WebView webView;
    private WebView mWebviewPop;
    private FrameLayout mContainer;
    public Context mContext;

    public String androidId;
    public String mainUrl;

    public String getMainUrl(){
        return mainUrl + "?platform=Android&id=" + androidId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        mainUrl = "file:///android_asset/demoDinomao/index.html";
        webView = (WebView) findViewById(R.id.web_view);
        mContainer = (FrameLayout) findViewById(R.id.webview_frame);

        WebView.setWebContentsDebuggingEnabled(true);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(true);
        settings.setDomStorageEnabled(true);
//        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        // settings.setSupportZoom(true);
        // settings.setBuiltInZoomControls(true);
        webView.setWebViewClient(webViewClient);
        webView.setWebChromeClient(new WebChromeClientForMain());

        AndroidLogger agloger = new AndroidLogger();
        webView.addJavascriptInterface(agloger, "androidLogger");
        agloger.webView = webView;

//        WebStorage.getInstance().deleteAllData();

        androidId = Settings.System.getString(getBaseContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        webView.loadUrl(getMainUrl());

        mContext = this.getApplicationContext();
    }

    @Override
    protected void onPause() {
        super.onPause();
        WebView webView = (WebView) findViewById(R.id.web_view);
        webView.loadUrl("javascript:androidPause()");
    }

    @Override
    protected void onResume() {
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

    class WebChromeClientForMain extends WebChromeClient {

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog,
                                      boolean isUserGesture, Message resultMsg) {
            mWebviewPop = new WebView(mContext);
            mWebviewPop.setVerticalScrollBarEnabled(false);
            mWebviewPop.setHorizontalScrollBarEnabled(false);
            mWebviewPop.setWebViewClient(new WebViewClientForMain());
            mWebviewPop.getSettings().setJavaScriptEnabled(true);
            mWebviewPop.getSettings().setSavePassword(false);
            mWebviewPop.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            mContainer.addView(mWebviewPop);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(mWebviewPop);
            resultMsg.sendToTarget();

            return true;
        }

        @Override
        public void onCloseWindow(WebView window) {
            Log.d("onCloseWindow", "called");
        }
    }

    class WebViewClientForMain extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {//ҳ��������
            // progressBar.setVisibility(view.GONE);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {//ҳ�濪ʼ����
            super.onPageStarted(view, url, favicon);
            System.out.println(url);

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            return false;
            System.out.println(111144);
            System.out.println(url);
//        if( url == mainUrl ) url = getMainUrl();

            //Log.d("shouldOverrideUrlLoading", url);
//            if (url.indexOf("file:///android_asset/demoDinomao/index.html?user_account_info") >= 0) {
//                // This is my web site, so do not override; let my WebView load
//                // the page
//                System.out.println(111145);
//
//                return false;
//            }
//
//            if (url.indexOf("m.facebook.com") >= 0) {
//                return false;
//            }
            // Otherwise, the link is not for a page on my site, so launch
            // another Activity that handles URLs
            return super.shouldOverrideUrlLoading(view, url);
//            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//            startActivity(intent);
//            return true;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                       SslError error) {
            Log.d("onReceivedSslError", "onReceivedSslError");
            //super.onReceivedSslError(view, handler, error);
        }
    }

    class AndroidLogger extends Object{
        public WebView webView;

        @JavascriptInterface
        public void log( String str ){
            System.out.println( str );
        }

        @JavascriptInterface
        public void backToLobby( String str ) {
            webView.post( new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("file:///android_asset/demoDinomao/index.html?" + str);

                    if (mWebviewPop != null) {
                        mWebviewPop.setVisibility(View.GONE);
                        mContainer.removeView(mWebviewPop);
                        System.out.println(111146);
                        mWebviewPop = null;
                    }
                }
            });
        }
    }
}