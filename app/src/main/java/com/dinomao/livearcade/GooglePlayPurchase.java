package com.dinomao.livearcade;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.singular.sdk.Singular;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GooglePlayPurchase {

    private static String purchaseId;
    private static int purchaseType;
    private static SkuDetails skuDetails;
    private static float price;

    private static GooglePlayPurchase currentPurchase;

    public static WebView webView;

    private PurchasesUpdatedListener purchasesUpdatedListener = new MyPurchasesUpdatedListener();

    private BillingClient billingClient;
    private Activity activity;

    public static FirebaseAnalytics mFirebaseAnalytics;

    public static GooglePlayPurchase createPurchase(String purchaseInfoString, Activity mActivity, WebView mainWebView ){
        webView = mainWebView;
        currentPurchase = new GooglePlayPurchase( purchaseInfoString, mActivity );
        buyPurchase();
        return currentPurchase;
    }

    public GooglePlayPurchase( String purchaseInfoString, Activity mActivity ){
        String[] purchaseInfo = purchaseInfoString.split(",");
        if( purchaseInfo.length != 2 ) {
            System.out.println( "purchase info error" );
            return;
        }
        this.purchaseId = purchaseInfo[0];
        this.purchaseType = Integer.parseInt( purchaseInfo[1] );

        billingClient = BillingClient.newBuilder(mActivity)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        activity = mActivity;

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                System.out.println( "connect" );
                System.out.println( billingResult.getResponseCode() );
                System.out.println( BillingClient.BillingResponseCode.OK );
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    System.out.println( "connect to google play server" );
                }
                else {
                    purchaseFaild();
                    return;
                }

                List<String> skuList = new ArrayList<>();
                System.out.println( "purchaseId:" + GooglePlayPurchase.purchaseId );
                System.out.println( "purchaseType:" + GooglePlayPurchase.purchaseType );
                skuList.add(GooglePlayPurchase.purchaseId);
                SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                params.setSkusList(skuList).setType( GooglePlayPurchase.purchaseType != 0 ? BillingClient.SkuType.SUBS : BillingClient.SkuType.INAPP );
                billingClient.querySkuDetailsAsync(params.build(),
                    new SkuDetailsResponseListener() {
                        @Override
                        public void onSkuDetailsResponse(BillingResult billingResult,
                                                         List<SkuDetails> skuDetailsList) {
                            System.out.println( skuDetailsList );
                            skuDetails = skuDetailsList.get(0);
                            String priceStr = skuDetails.getTitle();

                            try {
                                priceStr = priceStr.replaceFirst( "\\D", "");
                                priceStr = priceStr.replaceFirst( " \\D*", "");
                                price = Float.parseFloat(priceStr);
                            }
                            catch (Exception e){
                                System.out.println( "replaceFirst error" );
                            }
                        }
                    });
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                System.out.println("onBillingServiceDisconnected");
                purchaseFaild();
            }
        });
    }

    public static void buyPurchase(){
        Timer timer = new Timer();
        timer.schedule( new TimerTask() {
            @Override
            public void run() {
                if( GooglePlayPurchase.skuDetails != null ){
                    webView.post( new Runnable() {
                        @Override
                        public void run() {
                            System.out.println( "buy purchase" );
                            currentPurchase.buyPurchaseBySku();
                        }
                    });
                }
                else {
                    buyPurchase();
                }
            }
        }, 100);
    }

    void buyPurchaseBySku(){
        // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build();
        int responseCode = billingClient.launchBillingFlow(activity, billingFlowParams).getResponseCode();
        if( responseCode > 0 ) purchaseFaild();
    }

    public static void purchaseFaild(){
        webView.loadUrl("javascript:document.androidPurchase('faild')");
        skuDetails = null;
        currentPurchase = null;
        webView = null;
    }

    class MyPurchasesUpdatedListener implements PurchasesUpdatedListener{
        @Override
        public void onPurchasesUpdated( BillingResult billingResult, List<Purchase> purchases) {
            System.out.println( "onPurchasesUpdated" );

            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                    && purchases != null) {
                for (Purchase purchase : purchases) {
                    handlePurchase(purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
                System.out.println("user cancel");
                purchaseFaild();
            } else {
                // Handle any other error codes.
                purchaseFaild();
            }
        }

        void handlePurchase(Purchase purchase) {
            String purchaseStr = purchase.toString();
            System.out.println(purchaseStr);
            webView.loadUrl("javascript:document.androidPurchase('ok" + purchaseStr + "')");
            skuDetails = null;
            currentPurchase = null;
            webView = null;

            try {
                System.out.println("purchaseStr");
                System.out.println(price);
                Singular.revenue( "USD", price, purchase );
            }
            catch ( Exception e){
                System.out.println("purchaseStr error");
            }

            try{
                Bundle params = new Bundle();
                params.putString(FirebaseAnalytics.Param.CURRENCY, "USD");
                params.putDouble(FirebaseAnalytics.Param.VALUE, Double.parseDouble("" + price ) );
                mFirebaseAnalytics.logEvent( FirebaseAnalytics.Event.PURCHASE, params );
            }
            catch ( Exception e){
                System.out.println("purchaseReport error");
                System.out.println( e.getMessage() );
            }

            ConsumeParams consumeParams =
                    ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();

            ConsumeResponseListener listener = new ConsumeResponseListener() {
                @Override
                public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        System.out.println( "confirm purchase" );
                    }
                }
            };

            billingClient.consumeAsync(consumeParams, listener);
        }
    }
}
