package com.dinomao.livearcade;

import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class FirebaseMG {
    public static FirebaseAnalytics mFirebaseAnalytics;

    public static void purchase( String currency, Double price ) {
        try {
            Bundle params = new Bundle();
            params.putString(FirebaseAnalytics.Param.CURRENCY, currency);
            params.putDouble(FirebaseAnalytics.Param.VALUE, price);
            FirebaseMG.mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE, params);
        } catch (Exception e) {
            System.out.println("purchaseReport error");
            System.out.println(e.getMessage());
        }
    }

    public static void report( String eventName ){
        System.out.println( "report: " + eventName );
        try {
            FirebaseMG.mFirebaseAnalytics.logEvent(eventName, null);
        } catch (Exception e) {
            System.out.println( "event error: " + eventName );
        }
    }

    public static void report( String eventName, String extraStr ){
        System.out.println( "report: " + eventName );
        System.out.println( "extraStr: " + extraStr );
        try {
            Bundle params = new Bundle();
            params.putString(FirebaseAnalytics.Param.VALUE, extraStr);
            FirebaseMG.mFirebaseAnalytics.logEvent(eventName, params);
        } catch (Exception e) {
            System.out.println( "event error: " + eventName );
        }
    }

    public static void purchaseError( String reason ){
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.VALUE, reason);
        FirebaseMG.mFirebaseAnalytics.logEvent( "purchase_error", params);
    }
}
