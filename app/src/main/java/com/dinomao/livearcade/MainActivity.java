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
import android.view.KeyEvent;
import android.view.View;
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

import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AFInAppEventType;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.singular.sdk.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.appsflyer.AppsFlyerLib;
import com.appsflyer.attribution.AppsFlyerRequestListener;
import com.appsflyer.AppsFlyerConversionListener;

public class MainActivity extends AppCompatActivity {

    private WebViewClient webViewClient = new WebViewClientForMain();

    private WebView webView;
    private FrameLayout mContainer;
    public Context mContext;
    public Activity mActivity;

    public String androidId;
    public String mainUrl;

    public String getMainUrl(){
        return mainUrl + "?platform=Android&id=" + androidId;
    }

    public Boolean isLinkToToken = false;

    CallbackManager callbackManager;

    GoogleSignInClient mGoogleSignInClient;
    int RC_SIGN_IN = 2;

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

        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();

        androidId = Settings.System.getString(getBaseContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        webView.loadUrl( getMainUrl() + ( appLinkData != null ? "&linkData=" + appLinkData.toString() : "" ) );

        mActivity = this;
        mContext = this.getApplicationContext();

        initSingularSDK();

        FirebaseMG.mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
            new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    System.out.println( "try login 1" );
                    AccessToken accessToken = AccessToken.getCurrentAccessToken();
                    boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
                    String user_account_info = "platform=Android&sid=f4grk1ufogbq5ulmab43ud6oa5&access_token=" + accessToken.getToken();
                    user_account_info += "&expireTime=" + Math.round( accessToken.getExpires().getTime() / 1000 ) + ( isLinkToToken ? "&login_type=facebook_link_account" : "&login_type=facebook" );
                    webView.loadUrl(mainUrl + "?" + ( isLinkToToken ? "link_token=" : "user_account_info=" ) + user_account_info );
                    System.out.println( mainUrl + "?" +  ( isLinkToToken ? "link_token=" : "user_account_info=" ) + user_account_info );
                    isLinkToToken = false;
                }

                @Override
                public void onCancel() {
                    System.out.println( "try login 2" );
                    webView.loadUrl("javascript:alert('login failed')");
                }

                @Override
                public void onError(FacebookException exception) {
                    System.out.println( "try login 3" );
                    System.out.println( exception );
                    webView.loadUrl("javascript:alert('login failed')");
                }
            });
        LoginManager.getInstance().setLoginBehavior( LoginBehavior.WEB_ONLY );

        AppsFlyerConversionListener conversionListener =  new AppsFlyerConversionListener() {
            @Override
            public void onConversionDataSuccess(Map<String, Object> map) {
                for (String attrName : map.keySet())
                    System.out.println( "Conversion attribute: " + attrName + " = " + map.get(attrName) );
                String status = Objects.requireNonNull(map.get("af_status")).toString();
                if(status.equals("Organic")){
                    // Business logic for Organic conversion goes here.
                }
                else {
                    // Business logic for Non-organic conversion goes here.
                }
            }

            @Override
            public void onConversionDataFail(String errorMessage) {
                System.out.println( "error getting conversion data: " + errorMessage );
            }

            @Override
            public void onAppOpenAttribution(Map<String, String> attributionData) {
                // Must be overriden to satisfy the AppsFlyerConversionListener interface.
                // Business logic goes here when UDL is not implemented.
            }

            @Override
            public void onAttributionFailure(String errorMessage) {
                System.out.println( "error onAttributionFailure : " + errorMessage );
            }
        };

        AppsFlyerLib.getInstance().init("KfxGNL5BaQVRfXW77w25uT", conversionListener, this);
        AppsFlyerLib.getInstance().start( mContext, "KfxGNL5BaQVRfXW77w25uT", new AppsFlyerRequestListener() {
            @Override
            public void onSuccess() {
                System.out.println("Launch sent successfully, got 200 response code from server");
            }

            @Override
            public void onError(int i, String s) {
                System.out.println("Launch failed, Error code: " + i + "\n" + "Error description: " + s);
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        InstallReferrerUtil.setup( mContext );
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
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            System.out.println("登录成功");

            Thread tempThread = new Thread(){
                public void run(){
                    try {
                        String access_token = GoogleAuthUtil.getToken(mContext, account.getAccount(), "oauth2: email");
                        System.out.println("access_token: " + access_token );
                        String user_account_info = "platform=Android&sid=f4grk1ufogbq5ulmab43ud6oa5&access_token=" + access_token;
                        user_account_info += "&login_type=google";

                        final String userAccountStr = user_account_info;

                        webView.post( new Runnable() {
                            @Override
                            public void run() {
                                webView.loadUrl(mainUrl + "?user_account_info=" + userAccountStr );
                            }
                        });

                    }
                    catch ( Exception e ){
                        System.out.println("access_token error: " + e.getMessage() );
                    }
                }
            };
            tempThread.start();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            System.out.println("error: " + e.getStatusCode() );
            String status = e.getStatus().toString();
            webView.loadUrl("javascript:alert('" + status + ",code: " + e.getStatusCode() + "')");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            webView.loadUrl("javascript:document.back()");
            return true;
        }
        return super.onKeyDown( keyCode, event );
    }

    class WebChromeClientForMain extends WebChromeClient {

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog,
                                      boolean isUserGesture, Message resultMsg) {
            System.out.println( "create window: " );
            System.out.println( resultMsg );
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
            return true;
        }

        @Override
        public void onCloseWindow(WebView window) {
            Log.d("onCloseWindow", "called");
        }
    }

    class WebViewClientForMain extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            // progressBar.setVisibility(view.GONE);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
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
            System.out.println(str);
            if( str.equals( "link_token" ) ){
                isLinkToToken = true;
                return;
            }

            webView.post( new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl(mainUrl + "?" + InstallReferrerUtil.installReferrer() + "&" + str);
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
        public void exitApp( String str ){
            System.exit(0);
        }

        @JavascriptInterface
        public void report( String str ){
            System.out.println( "report: " + str );
            try {
                String[] eventStrings = str.split("_");
                String cmd = eventStrings[0];
                switch (cmd) {
                    case "First Login":
                        Singular.event("First Login", "id", eventStrings[1]);
                        break;
                    case "buySuccess":
                        Singular.event("buySuccess", "id", eventStrings[1], "type", eventStrings[2], "price", eventStrings[3]);

                        Map<String, Object> eventValues = new HashMap<String, Object>();
                        eventValues.put(AFInAppEventParameterName.REVENUE, eventStrings[3]);
                        eventValues.put(AFInAppEventParameterName.CURRENCY, "USD");
                        AppsFlyerLib.getInstance().logEvent(mContext, AFInAppEventType.PURCHASE, eventValues);

                        break;
                    case "no video":
                        FirebaseMG.report( "No_video" );
                        break;
                    case "banner click":
                    case "click the category":
                    case "exchange price for ticket":
                    case "exchange ticket for price":
                    case "leave room without play":
                    case "Loading time":
                    case "enter machine":
                        FirebaseMG.report( cmd.replace( " ", "_" ), eventStrings[2] );
                        break;
                    case "game replay":
                    case "purchase through play button":
                    case "purchase through coin button":
                    case "connect error":
                    case "stream error during play":
                        FirebaseMG.report( cmd.replace( " ", "_" ) );
                        break;
                    case "ftd":
                        AppsFlyerLib.getInstance().logEvent(mContext,cmd,new HashMap<>());
                        break;
                    default:
                        System.out.println( "unknow report event: " + str );
                        break;
                }
            }
            catch ( Exception e ){
                webView.post( new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl("javascript:alert('Singular report mistake');");
                    }
                });
            }
        }

        @JavascriptInterface
        public void share( String str ){
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, str);
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        }

        @JavascriptInterface
        public void facebookLogin( String str ){
            LoginManager.getInstance().logInWithReadPermissions( mActivity, Arrays.asList("public_profile") );
        }
    }
}