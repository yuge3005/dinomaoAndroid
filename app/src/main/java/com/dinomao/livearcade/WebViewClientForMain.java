package com.dinomao.livearcade;

import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.logging.Logger;

public class WebViewClientForMain extends WebViewClient {
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
//            return false;
        System.out.println(111144);
        return super.shouldOverrideUrlLoading(view, url);

    }
}
