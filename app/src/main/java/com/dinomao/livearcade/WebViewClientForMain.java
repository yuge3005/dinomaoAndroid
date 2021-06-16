package com.dinomao.livearcade;

import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.logging.Logger;

public class WebViewClientForMain extends WebViewClient {

    public static String androidId;
    public static String mainUrl;

    public static String getMainUrl(){
        return mainUrl + "?platform=Android&id=" + androidId;
    }

    @Override
    public void onPageFinished(WebView view, String url) {//ҳ��������
        // progressBar.setVisibility(view.GONE);
        System.out.println(111122);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {//ҳ�濪ʼ����
        super.onPageStarted( view, url, favicon );
        System.out.println(111133);
        System.out.println(url);

    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            return false;
        System.out.println(111144);
        System.out.println(url);
        if( url == mainUrl ) url = getMainUrl();
        return super.shouldOverrideUrlLoading(view, url);

    }
}
