package com.dinomao.livearcade;

import android.app.Activity;

import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

public class GooglePlayPurchase {

    private static String purchaseId;
    private static int purchaseType;

    private PurchasesUpdatedListener purchasesUpdatedListener = new MyPurchasesUpdatedListener();

    private BillingClient billingClient;

    public static GooglePlayPurchase createPurchase(String purchaseInfoString, Activity mActivity ){
        return new GooglePlayPurchase( purchaseInfoString, mActivity );
    }

    public GooglePlayPurchase( String purchaseInfoString, Activity mActivity ){
        String[] purchaseInfo = purchaseInfoString.split(",");
        if( purchaseInfo.length != 2 ) {
            System.out.print( "purchase info error" );
            return;
        }
        this.purchaseId = purchaseInfo[0];
        this.purchaseType = Integer.parseInt( purchaseInfo[1] );

        System.out.println(purchaseId);

        billingClient = BillingClient.newBuilder(mActivity)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

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
                            }
                        });
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }

    class MyPurchasesUpdatedListener implements PurchasesUpdatedListener{
        @Override
        public void onPurchasesUpdated( BillingResult billingResult, List<Purchase> list) {

        }
    }
}
