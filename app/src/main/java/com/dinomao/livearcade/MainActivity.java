package com.dinomao.livearcade;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
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
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.graphics.Bitmap;
import android.content.res.Resources;
import android.widget.FrameLayout;

import com.singular.sdk.*;

public class MainActivity extends AppCompatActivity {

    private WebViewClient webViewClient = new WebViewClientForMain();

    private WebView webView;
    private WebView mWebviewPop;
    private FrameLayout mContainer;
    public Context mContext;
    public Activity mActivity;

    public String androidId;
    public String mainUrl;

    public String getMainUrl(){
        return mainUrl + "?platform=Android&id=" + androidId;
    }

    private Bundle deeplinkData;

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
        settings.setAllowFileAccess(true);
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

        mActivity = this;
        mContext = this.getApplicationContext();

        initSingularSDK();
    }

    private void initSingularSDK() {
//        SingularConfig config = new SingularConfig(Constants.API_KEY, Constants.SECRET).withSingularLink(getIntent(), new SingularLinkHandler() {
//            @Override
//            public void onResolved(SingularLinkParams singularLinkParams) {
//                deeplinkData = new Bundle();
//                deeplinkData.putString(Constants.DEEPLINK_KEY, singularLinkParams.getDeeplink());
//                deeplinkData.putString(Constants.PASSTHROUGH_KEY, singularLinkParams.getPassthrough());
//                deeplinkData.putBoolean(Constants.IS_DEFERRED_KEY, singularLinkParams.isDeferred());

                // When the is opened using a deeplink, we will open the deeplink tab
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        tabLayout.getTabAt(3).select();
//                    }
//                });
//            }
//        });

        SingularConfig config = new SingularConfig(Constants.API_KEY, Constants.SECRET).withCustomUserId(androidId);

        Singular.init(this, config);
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.loadUrl("javascript:document.appPause()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.loadUrl("javascript:document.appResume()");
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
            System.out.println( "goto new page:" + url );
            if( url.startsWith( "newtab:" ) ){
                url = url.replace( "newtab:", "" );
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
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
                    webView.loadUrl(mainUrl + "?" + str);

                    if (mWebviewPop != null) {
                        mWebviewPop.setVisibility(View.GONE);
                        mContainer.removeView(mWebviewPop);
                        System.out.println(111146);
                        mWebviewPop = null;
                    }
                }
            });
        }

        @JavascriptInterface
        public void purchase( String str ){
            System.out.println( "purchase" );
            GooglePlayPurchase.createPurchase(str, mActivity, webView);
        }

        @JavascriptInterface
        public void needActive( String str ){
            System.out.println( "needActive" );
            webView.post( new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:document.body.bgcolor");
                }
            });
        }

        @JavascriptInterface
        public void video( String str ){
            System.out.println( "video：" );
            System.out.println( str );
            try{
                int a = Integer.valueOf( str );
                webView.post( new Runnable() {
                    @Override
                    public void run() {
                        System.out.println( "videoFrame" );
                        webView.loadUrl("javascript:document.getElementById('videoFrame').contentWindow.playVideo(" + a + ")");
                    }
                });
            }
            catch ( Exception e ){
                System.out.println( "video message" );
                System.out.println( str );
            }
        }
    }
}