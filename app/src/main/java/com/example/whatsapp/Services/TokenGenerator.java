package com.example.whatsapp.Services;

import android.util.Log;

import io.agora.media.RtcTokenBuilder2;
import io.agora.media.RtcTokenBuilder2.Role;


public class TokenGenerator {

    static String appId;
    static String appCertificate;
    static String channelName;
    static int uid = 0; // The integer uid, required for an RTC token
    static int expirationTimeInSeconds = 3600; // The time after which the token expires

    public static String generateToken(String callerChannelName, String agoraAppId, String agoraAppCertificate) throws Exception {

        channelName = callerChannelName;
        appId = agoraAppId;
        appCertificate = agoraAppCertificate;

        RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();
        // Calculate the time expiry timestamp
        int timestamp = (int)(System.currentTimeMillis() / 1000 + expirationTimeInSeconds);

        String result = tokenBuilder.buildTokenWithUid(appId, appCertificate, channelName, uid, Role.ROLE_PUBLISHER, timestamp, timestamp);
        Log.d("tokenDebug", result);
        return result;
    }
}
