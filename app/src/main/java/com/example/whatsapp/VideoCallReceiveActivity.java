package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.whatsapp.APIs.JavaScriptInterface;
import com.example.whatsapp.Models.Users;
import com.example.whatsapp.databinding.ActivityVideoCallReceiveBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ktx.Firebase;
import com.squareup.picasso.Picasso;

public class VideoCallReceiveActivity extends AppCompatActivity {

    ActivityVideoCallReceiveBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    Users user;

    String callerId, callerUserName, profileImageURI, senderId;
    Boolean isMute = false, isPeerConnected = false, isAudio = true, isVideo = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        binding = ActivityVideoCallReceiveBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        callerId = getIntent().getStringExtra("callerId");
        senderId = auth.getUid();

        database.getReference().child("Users").child(callerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(Users.class);
                profileImageURI = user.getProfilepic();
                callerUserName = user.getUserName();

                binding.tvCallerName.setText(callerUserName);
                Picasso.get().load(profileImageURI).placeholder(R.drawable.profile).into(binding.profileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.btnVoiceMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isMute = !isMute;
                if(isMute) {
                    binding.btnVoiceMute.setImageResource(R.drawable.volume_off);
                } else {
                    binding.btnVoiceMute.setImageResource(R.drawable.volume_up);
                }
            }
        });

        binding.btnAcceptCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.videoCallReceiveLayout.setVisibility(View.GONE);
                binding.videoCallLayout.setVisibility(View.VISIBLE);
                setUpWebView();
            }
        });

        binding.btnRejectCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.getReference().child("Users").child(callerId).child("callConnectionId").setValue("null");
                database.getReference().child("Users").child(senderId).child("callConnectionId").setValue("null");
                database.getReference().child("Users").child(senderId).child("isAvailableForCalls").setValue(false);
                database.getReference().child("Users").child(senderId).child("incomingVideoCall").setValue("null");
                onBackPressed();
            }
        });

        binding.btnCameraOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isVideo = !isVideo;
                callJavaScriptFunction("toggleVideo('" + isVideo + "')");
                if (isVideo) {
                    binding.btnCameraOff.setImageResource(R.drawable.camera_off_whitebg);
                } else {
                    binding.btnCameraOff.setImageResource(R.drawable.camera_whitebg);
                }
            }
        });

        binding.btnMicOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAudio = !isAudio;
                callJavaScriptFunction("toggleAudio('" + isAudio + "')");
                if (isAudio) {
                    binding.btnMicOff.setImageResource(R.drawable.mic_off_whitebg);
                } else {
                    binding.btnMicOff.setImageResource(R.drawable.mic_on_whitebg);
                }
            }
        });

        binding.btnCallEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.getReference().child("Users").child(callerId).child("callConnectionId").setValue("null");
                database.getReference().child("Users").child(senderId).child("callConnectionId").setValue("null");
                database.getReference().child("Users").child(senderId).child("isAvailableForCalls").setValue(false);
                database.getReference().child("Users").child(senderId).child("incomingVideoCall").setValue("null");
                onBackPressed();
            }
        });
    }

    private void setUpWebView() {

        binding.webView.setWebChromeClient(new VideoCallReceiveActivity.CustomWebChromeClient());
        // Enable JavaScript in WebView
        binding.webView.getSettings().setJavaScriptEnabled(true);   //Warning says that there may be vulnerabilities in the js file... since we have coded the web content ourselves... we can choose to ignore this warning
        binding.webView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        // Create a JavaScript interface object
//        JavaScriptInterface jsInterface = new JavaScriptInterface();
//        // Add the JavaScript interface to the WebView
//        binding.webView.addJavascriptInterface(jsInterface, "AndroidInterface");

        loadVideoCall();

    }

    private void loadVideoCall() {

        try {
            binding.webView.loadUrl("file:///android_asset/index.html");
            Log.d("vcDebug","WebView LocalURL loaded");

            binding.webView.setWebViewClient(new WebViewClient(){
                @Override
                public void onPageFinished(WebView view, String url) {
                    Log.d("vcDebug","OnPageFinished Called");
                    super.onPageFinished(view, url);
                    // This method will be called when the web page has completely loaded
                    initializePeer();
                }
            });
        } catch (Exception e) {
            Log.e("vcDebug",Log.getStackTraceString(e));
        }
    }

    private void initializePeer() {

        binding.webView.evaluateJavascript("init('" + senderId + "')", null);
        Log.d("vcDebug","JavaScript init function called");
        onPeerConnected();
        Log.d("vcDebug","" + isPeerConnected);
        acceptIncomingCall();
        Log.d("vcDebug","incoming call accepted...");

    }

    public void onPeerConnected() {
        isPeerConnected = true;
    }

    private void acceptIncomingCall() {

        database.getReference().child("Users").child(senderId).child("callConnectionId").setValue(callerId+senderId);
        database.getReference().child("Users").child(senderId).child("isAvailableForCalls").setValue(true);

    }

    private static class CustomWebChromeClient extends WebChromeClient {
        @Override
        public void onPermissionRequest(final PermissionRequest request) {
            // Check the requested permissions
            String[] requestedPermissions = request.getResources();
            for (String permission : requestedPermissions) {
                // Handle the requested permissions as needed
                if (permission.equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                    // Grant permission for video capture
                    request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
                } else if (permission.equals(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
                    // Grant permission for audio capture
                    request.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});
                } else {
                    // Deny permission for other resources
                    request.deny();
                }
            }
        }
    }

    private void callJavaScriptFunction(String functionName) {
        // Since calling the JS functions from server.js can't be run on Main Thread... we take the help of child threads
        binding.webView.post(new Runnable() {
            @Override
            public void run() {
                binding.webView.evaluateJavascript(functionName, null);
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        binding.webView.loadUrl("about:blank");
        binding.videoCallReceiveLayout.setVisibility(View.VISIBLE);
        binding.videoCallLayout.setVisibility(View.GONE);
        super.onDestroy();
    }
}