package com.dinomao.livearcade;

import android.app.Activity;

import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;

import java.util.List;

public class GooglePlayPurchase {

    private String purchaseId;

    private PurchasesUpdatedListener purchasesUpdatedListener = new MyPurchasesUpdatedListener();

    private BillingClient billingClient = BillingClient.newBuilder(new Activity())
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build();

    public static GooglePlayPurchase createPurchase(String purchaseId ){
        return new GooglePlayPurchase( purchaseId );
    }

    public GooglePlayPurchase( String purchaseId ){
        this.purchaseId = purchaseId;

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                }
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
