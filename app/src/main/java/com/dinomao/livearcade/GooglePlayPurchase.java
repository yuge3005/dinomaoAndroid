package com.dinomao.livearcade;

public class GooglePlayPurchase {

    private String purchaseId;

    public static GooglePlayPurchase createPurchase( String purchaseId ){
        return new GooglePlayPurchase( purchaseId );
    }

    public GooglePlayPurchase( String purchaseId ){
        this.purchaseId = purchaseId;
    }
}
