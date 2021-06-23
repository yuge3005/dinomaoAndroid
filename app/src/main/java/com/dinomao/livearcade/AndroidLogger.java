package com.dinomao.livearcade;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;

public class AndroidLogger extends Object{
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
            }
        });
    }
}