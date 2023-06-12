package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;

import com.example.whatsapp.APIs.TokenGenerator;
import com.example.whatsapp.Models.Users;
import com.example.whatsapp.databinding.ActivityVideoCallBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.ChannelMediaOptions;

public class VideoCallActivity extends AppCompatActivity {

    ActivityVideoCallBinding binding;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();

//    Properties properties = new Properties();
//    AssetManager assetManager = getAssets();

    // Fill the App ID of your project generated on Agora Console.
    private final String appId = "ec3d7fe7796e45c6bef121efb7f11d4c";
    // Fill the channel name.
    private String channelName = "";
    // Fill the temp token generated on Agora Console.
    private String token = "";
    // An integer that identifies the local user.
    private int uid = 0;
    private boolean isJoined = false;

    private RtcEngine agoraEngine;
    //SurfaceView to render local video in a Container.
    private SurfaceView localSurfaceView;
    //SurfaceView to render Remote video in a Container.
    private SurfaceView remoteSurfaceView;

    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS =
            {
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
            };
    //Auth id of the person who is to be called... this token is received from the caller
    String receiverId = "";
    Boolean receiverAvailableForCalls = false;
    int SR_TOKEN;  // caller - 1   receiver - 2
    int EXECUTION_TOKEN = 1; //RUN = 1  & STOP = 0

    //one who makes the call
    String callerId = "";

    private boolean checkSelfPermission()
    {
        if (ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) !=  PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[1]) !=  PackageManager.PERMISSION_GRANTED)
        {
            return false;
        }
        return true;
    }

    void showMessage(String message) {
        runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }


    private void setupVideoSDKEngine() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = VideoCallActivity.this;
            config.mAppId = appId;
            config.mEventHandler = mRtcEventHandler;
            agoraEngine = RtcEngine.create(config);
            // By default, the video module is disabled, call enableVideo to enable it.
            agoraEngine.enableVideo();
        } catch (Exception e) {
            showMessage(e.toString());
        }
    }

    private void setupRemoteVideo(int uid) {
        FrameLayout container = findViewById(R.id.remote_video_view_container);
        remoteSurfaceView = new SurfaceView(VideoCallActivity.this);
        remoteSurfaceView.setZOrderMediaOverlay(true);
        container.addView(remoteSurfaceView);
        agoraEngine.setupRemoteVideo(new VideoCanvas(remoteSurfaceView, VideoCanvas.RENDER_MODE_FIT, uid));
        // Display RemoteSurfaceView.
        remoteSurfaceView.setVisibility(View.VISIBLE);
    }

    private void setupLocalVideo() {
        FrameLayout container = findViewById(R.id.local_video_view_container);
        // Create a SurfaceView object and add it as a child to the FrameLayout.
        localSurfaceView = new SurfaceView(VideoCallActivity.this);
        container.addView(localSurfaceView);
        // Call setupLocalVideo with a VideoCanvas having uid set to 0.
        agoraEngine.setupLocalVideo(new VideoCanvas(localSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        // Listen for the remote host joining the channel to get the uid of the host.
        public void onUserJoined(int uid, int elapsed) {
            showMessage("Remote user joined " + uid);

            // Set the remote video view
            runOnUiThread(() -> setupRemoteVideo(uid));
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            isJoined = true;
            showMessage("Joined Channel " + channel);
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            showMessage("Remote user offline " + uid + " " + reason);
            runOnUiThread(() -> remoteSurfaceView.setVisibility(View.GONE));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityVideoCallBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        receiverId = getIntent().getStringExtra("receiverId");
        callerId = getIntent().getStringExtra("callerId");
        SR_TOKEN =  getIntent().getIntExtra("srToken", 0 );
        Log.d("videoCallDebug", receiverId);

        //Creating a unique channel name and generating a token - valid for an hour
        channelName = callerId+receiverId;
        try {
            token = TokenGenerator.generateToken(channelName);
        } catch (Exception e) {
            Toast.makeText(this, "Token Generation Failed. Exiting", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(VideoCallActivity.this, MainActivity.class);
            intent.putExtra("intentToken", 1);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            database.getReference().child("Users").child(receiverId).child("incomingVideoCall").setValue("null");
            database.getReference().child("Users").child(receiverId).child("isAvailableForCalls").setValue(false);
            EXECUTION_TOKEN = 0; // STOP Runnable
            startActivity(intent);
            Toast.makeText(getApplicationContext(), "Video Chat Ended", Toast.LENGTH_SHORT).show();
            finish();
        }

        if(SR_TOKEN == 1) { //CALLER
            database.getReference().child("Users").child(receiverId).child("userName").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String receiverUserName = snapshot.getValue(String.class);
                    binding.tvRemoteUserName.setText(receiverUserName);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("videoCallDebug", "Unable to fetch Receiver User Name... FirebaseError");
                }
            });
        } else if(SR_TOKEN==2) {
            database.getReference().child("Users").child(callerId).child("userName").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String receiverUserName = snapshot.getValue(String.class);
                    binding.tvRemoteUserName.setText(receiverUserName);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("videoCallDebug", "Unable to fetch Receiver User Name... FirebaseError");
                }
            });
        }

        if(!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        }

        Log.d("videoCallDebug", "setupVideoSDKEngine called ");
        setupVideoSDKEngine();
        //Code to Establish Call Directly
        joinCall();

        binding.btnCallEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveCall();
                Intent intent = new Intent(VideoCallActivity.this, MainActivity.class);
                intent.putExtra("intentToken", 1);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                database.getReference().child("Users").child(receiverId).child("incomingVideoCall").setValue("null");
                database.getReference().child("Users").child(receiverId).child("isAvailableForCalls").setValue(false);
                if(SR_TOKEN==1)
                    EXECUTION_TOKEN = 0; // STOP Runnable
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "Video Chat Ended", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

//        if(!receiverAvailableForCalls) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // Perform the subsequent code
                    if(EXECUTION_TOKEN==1) {
                        database.getReference().child("Users").child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Users user = snapshot.getValue(Users.class);
                                assert user != null;
                                Boolean value = user.getAvailableForCalls();
                                String incomingCall = user.getIncomingVideoCall();
                                if (incomingCall.equals("null")) {
                                    leaveCall();
                                    Intent intent = new Intent(VideoCallActivity.this, MainActivity.class);
                                    intent.putExtra("intentToken", 1);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    if(SR_TOKEN ==1)
                                        Toast.makeText(getApplicationContext(), "Receiver Ended The Call", Toast.LENGTH_LONG).show();
                                    else if(SR_TOKEN ==2)
                                        Toast.makeText(getApplicationContext(), "You Ended The Call", Toast.LENGTH_LONG).show();
                                    timer.cancel();
                                    finish();
                                } else if (Boolean.TRUE.equals(value)) {
                                    receiverAvailableForCalls = true;
                                    Toast.makeText(getApplicationContext(), "Call in Progress...", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            },0, 5000);
        }
//    }

    private void leaveCall() {

        if(!isJoined) {
            showMessage("Join Our Channel First");
        } else {
            agoraEngine.leaveChannel();
            showMessage("Left the Channel. Call Ended");
            // Stop remote video rendering.
            if(remoteSurfaceView != null )
                remoteSurfaceView.setVisibility(View.GONE);
            // Stop local video rendering.
            if(localSurfaceView != null )
                localSurfaceView.setVisibility(View.GONE);
            isJoined = false;
        }
    }

    private void joinCall() {

        if (checkSelfPermission()) {
            ChannelMediaOptions option = new ChannelMediaOptions();
            // For a Video call, set the channel profile as COMMUNICATION.
            option.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION;
            // Set the client role as BROADCASTER or AUDIENCE according to the scenario.
            option.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
            // Display LocalSurfaceView.
            setupLocalVideo();
            localSurfaceView.setVisibility(View.VISIBLE);
            // Start local preview.
            agoraEngine.startPreview();
            // Join the channel with a temp token.
            // You need to specify the user ID yourself, and ensure that it is unique in the channel.
            agoraEngine.joinChannel(token, channelName, uid, option);
        }else {
            Toast.makeText(getApplicationContext(), "Permission Not Granted", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        agoraEngine.stopPreview();
        agoraEngine.leaveChannel();

        // Destroy the engine in a sub-thread to avoid congestion
        new Thread(() -> {
            RtcEngine.destroy();
            agoraEngine = null;
        }).start();

    }
}