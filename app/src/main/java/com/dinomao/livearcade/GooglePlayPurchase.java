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

    private String purchaseId;

    private PurchasesUpdatedListener purchasesUpdatedListener = new MyPurchasesUpdatedListener();

    private BillingClient billingClient;

    public static GooglePlayPurchase createPurchase(String purchaseId, Activity mActivity ){
        return new GooglePlayPurchase( purchaseId, mActivity );
    }

    public GooglePlayPurchase( String purchaseId, Activity mActivity ){
        this.purchaseId = purchaseId;

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
                skuList.add("premium_upgrade");
                skuList.add("gas");
                SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
                billingClient.querySkuDetailsAsync(params.build(),
                        new SkuDetailsResponseListener() {
                            @Override
                            public void onSkuDetailsResponse(BillingResult billingResult,
                                                             List<SkuDetails> skuDetailsList) {
                                System.out.println( "skuDetailsList" );
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
