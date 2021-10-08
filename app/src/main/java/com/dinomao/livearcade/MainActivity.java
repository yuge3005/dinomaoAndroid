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

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.LoginStatusCallback;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.singular.sdk.*;

import java.util.Arrays;

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

    CallbackManager callbackManager;

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

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
            new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    System.out.println( "try login 1" );
                }

                @Override
                public void onCancel() {
                    System.out.println( "try login 2" );
                }

                @Override
                public void onError(FacebookException exception) {
                    System.out.println( "try login 3" );
                }
            });
    }

    private void initSingularSDK() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    class WebChromeClientForMain extends WebChromeClient {

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog,
                                      boolean isUserGesture, Message resultMsg) {
            LoginManager.getInstance().logInWithReadPermissions( mActivity, Arrays.asList("public_profile", "user_friends", "email") );
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

        @JavascriptInterface
        public void report( String str ){
            try {
                String[] eventStrings = str.split("_");
                String cmd = eventStrings[0];
                switch (cmd) {
                    case "First Login":
                        Singular.event("First Login", eventStrings[1]);
                        break;
                    case "buySuccess":
                        Singular.event("buySuccess", "id", eventStrings[1], "type", eventStrings[2], "price", eventStrings[3]);
                        break;
                }
            }
            catch ( Exception e ){
                webView.post( new Runnable() {
                    @Override
                    public void run() {
                        System.out.println( str );
                        webView.loadUrl("javascript:alert('Singular report mistake');");
                    }
                });
            }
        }
    }
}