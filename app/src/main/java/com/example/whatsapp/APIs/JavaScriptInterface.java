package com.example.whatsapp.APIs;

import android.webkit.JavascriptInterface;

import com.example.whatsapp.VideoCallActivity;
import com.example.whatsapp.VideoCallReceiveActivity;

public class JavaScriptInterface {

    VideoCallActivity videoCallActivity = new VideoCallActivity();
    VideoCallReceiveActivity videoCallReceiveActivity = new VideoCallReceiveActivity();

    @JavascriptInterface
    public void onPeerConnected() {
        videoCallActivity.onPeerConnected();
    }

}
