package com.dinomao.livearcade;

import android.webkit.JavascriptInterface;

public class AndroidLogger extends Object{
    @JavascriptInterface
    public void log( String str ){
        System.out.println( str );
    }
}