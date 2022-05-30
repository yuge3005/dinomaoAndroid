package com.dinomao.livearcade;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.RemoteException;
import android.text.TextUtils;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;

import java.util.Iterator;

import io.reactivex.android.schedulers.AndroidSchedulers;

public class InstallReferrerUtil {
    private final static String INSTALL_REFERRER = "com.android.vending.INSTALL_REFERRER";

    private static String _installReferrer;
    public static String installReferrer() {
        return _installReferrer;
    }

    public static void setup(Context context) {
        InstallReferrerClient client = InstallReferrerClient.newBuilder(context).build();
        client.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {
                if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                    try {
                        ReferrerDetails response = client.getInstallReferrer();
                        String referrerUrl = response.getInstallReferrer();
                        System.out.println( "referrerUrl: " + referrerUrl );
                        _installReferrer = referrerUrl;
                    } catch (RemoteException e) {
                        System.out.println( "RemoteException" );
                    }
                }
            }
            @Override
            public void onInstallReferrerServiceDisconnected() {
                System.out.println( "onInstallReferrerServiceDisconnected" );
            }
        });
    }
}
