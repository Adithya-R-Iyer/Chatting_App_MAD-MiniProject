package com.example.whatsapp.APIs;

import android.util.Log;

import io.agora.media.RtcTokenBuilder2;
import io.agora.media.RtcTokenBuilder2.Role;


public class TokenGenerator {

    static String appId = "ec3d7fe7796e45c6bef121efb7f11d4c";
    static String appCertificate = "0c3dc0998b6b412289ca1f1c21c9c959";
    static String channelName;
    static int uid = 0; // The integer uid, required for an RTC token
    static int expirationTimeInSeconds = 3600; // The time after which the token expires

    public static String generateToken(String callerChannelName) throws Exception {

        channelName = callerChannelName;

        RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();
        // Calculate the time expiry timestamp
        int timestamp = (int)(System.currentTimeMillis() / 1000 + expirationTimeInSeconds);

        String result = tokenBuilder.buildTokenWithUid(appId, appCertificate, channelName, uid, Role.ROLE_PUBLISHER, timestamp, timestamp);
        Log.d("tokenDebug", result);
        return result;
    }
}
