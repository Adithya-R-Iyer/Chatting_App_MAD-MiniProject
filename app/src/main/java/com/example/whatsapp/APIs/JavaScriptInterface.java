package com.example.whatsapp.APIs;

import android.webkit.JavascriptInterface;

import com.example.whatsapp.VideoCallActivity;

public class JavaScriptInterface {

    VideoCallActivity videoCallActivity = new VideoCallActivity();

    @JavascriptInterface
    public void onPeerConnected() {
        videoCallActivity.onPeerConnected();
    }

}
