package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.whatsapp.Services.SecretsManager;
import com.example.whatsapp.Services.TokenGenerator;
import com.example.whatsapp.Models.Users;
import com.example.whatsapp.databinding.ActivityVideoCallBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.Objects;
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

    // Fill the App ID of your project generated on Agora Console.
    private String appId = "";
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
    Boolean isAudio = true;
    Boolean isVideo = true;

    //one who makes the call
    String callerId = "";
    String callerProfilePicUri, receiverProfilePicUri;

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
        agoraEngine.setupRemoteVideo(new VideoCanvas(remoteSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
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

        appId = SecretsManager.readSecrets(getApplicationContext(), "agoraAppId");
        String agoraAppCertificate = SecretsManager.readSecrets(getApplicationContext(), "agoraAppCertificate");

        //Creating a unique channel name and generating a token - valid for an hour
        channelName = callerId+receiverId;
        try {
            token = TokenGenerator.generateToken(channelName, appId, agoraAppCertificate);
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

        database.getReference().child("Users").child(callerId).child("profilepic").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callerProfilePicUri = snapshot.getValue(String.class);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        database.getReference().child("Users").child(receiverId).child("profilepic").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                receiverProfilePicUri = snapshot.getValue(String.class);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        if(!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        }

        Log.d("videoCallDebug", "setupVideoSDKEngine called ");
        setupVideoSDKEngine();
        //Code to Establish Call Directly
        joinCall();

        //Listening to VIDEO AND AUDIO TOGGLE
        if(Objects.equals(auth.getUid(), callerId)) {
            database.getReference().child("Users").child(receiverId).child("toggleVideo").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Boolean toggleVid = snapshot.getValue(Boolean.class);
                    if(Boolean.TRUE.equals(toggleVid)) {
                        Picasso.get().load(receiverProfilePicUri).placeholder(R.drawable.profile).into(binding.remoteVideoViewBg);
                        binding.remoteVideoViewBg.setVisibility(View.VISIBLE);
                        binding.remoteVideoViewContainer.setVisibility(View.GONE);
                    } else if(Boolean.FALSE.equals(toggleVid)){
                        binding.remoteVideoViewBg.setVisibility(View.GONE);
                        binding.remoteVideoViewContainer.setVisibility(View.VISIBLE);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        } else if(Objects.equals(auth.getUid(), receiverId)) {
            database.getReference().child("Users").child(callerId).child("toggleVideo").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Boolean toggleVid = snapshot.getValue(Boolean.class);
                    if(Boolean.TRUE.equals(toggleVid)) {
                        Picasso.get().load(callerProfilePicUri).placeholder(R.drawable.profile).into(binding.remoteVideoViewBg);
                        binding.remoteVideoViewBg.setVisibility(View.VISIBLE);
                        binding.remoteVideoViewContainer.setVisibility(View.GONE);
                    } else if(Boolean.FALSE.equals(toggleVid)){
                        binding.remoteVideoViewBg.setVisibility(View.GONE);
                        binding.remoteVideoViewContainer.setVisibility(View.VISIBLE);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }


        binding.btnCallEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveCall();
                Intent intent = new Intent(VideoCallActivity.this, MainActivity.class);
                intent.putExtra("intentToken", 1);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                database.getReference().child("Users").child(receiverId).child("incomingVideoCall").setValue("null");
                database.getReference().child("Users").child(receiverId).child("isAvailableForCalls").setValue(false);
                database.getReference().child("Users").child(callerId).child("toggleVideo").setValue(false);
                database.getReference().child("Users").child(callerId).child("toggleAudio").setValue(false);
                database.getReference().child("Users").child(receiverId).child("toggleVideo").setValue(false);
                database.getReference().child("Users").child(receiverId).child("toggleAudio").setValue(false);
                if(SR_TOKEN==1)
                    EXECUTION_TOKEN = 0; // STOP Runnable
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "Video Chat Ended", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        binding.btnCameraToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isVideo = !isVideo;
//                agoraEngine.muteLocalVideoStream(isVideo);
                if (isVideo) {

                    binding.btnCameraToggle.setImageResource(R.drawable.camera_whitebg);
                    agoraEngine.muteLocalVideoStream(false); // Resume local video stream
                    agoraEngine.muteRemoteVideoStream(uid, false); // Resume remote video stream

                    if(auth.getUid().equals(callerId)) {
                        database.getReference().child("Users").child(callerId).child("toggleVideo").setValue(false);
                    } else if(auth.getUid().equals(receiverId)) {
                        database.getReference().child("Users").child(receiverId).child("toggleVideo").setValue(false);
                    }
                    binding.localVideoViewBg.setVisibility(View.GONE);
                    binding.localVideoViewContainer.setVisibility(View.VISIBLE);

                } else {

                    binding.btnCameraToggle.setImageResource(R.drawable.camera_off_whitebg);
                    agoraEngine.muteLocalVideoStream(true); // Pause local video stream
                    agoraEngine.muteRemoteVideoStream(uid, true); // Pause remote video stream

                    if(auth.getUid().equals(callerId)) {
                        Picasso.get().load(callerProfilePicUri).placeholder(R.drawable.profile).into(binding.localVideoViewBg);
                        database.getReference().child("Users").child(callerId).child("toggleVideo").setValue(true);
                    } else if(auth.getUid().equals(receiverId)) {
                        Picasso.get().load(receiverProfilePicUri).placeholder(R.drawable.profile).into(binding.localVideoViewBg);
                        database.getReference().child("Users").child(receiverId).child("toggleVideo").setValue(true);
                    }
                    binding.localVideoViewBg.setVisibility(View.VISIBLE);
                    binding.localVideoViewContainer.setVisibility(View.GONE);

                }
            }
        });

        binding.btnMicToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAudio = !isAudio;
                if (isAudio) {
                    binding.btnMicToggle.setImageResource(R.drawable.mic_on_whitebg);
                    agoraEngine.muteLocalAudioStream(false);
                    agoraEngine.muteRemoteAudioStream(uid, false);

                    if(auth.getUid().equals(callerId)) {
                        database.getReference().child("Users").child(callerId).child("toggleAudio").setValue(false);
                    } else if(auth.getUid().equals(receiverId)) {
                        database.getReference().child("Users").child(receiverId).child("toggleAudio").setValue(false);
                    }
                    Toast.makeText(VideoCallActivity.this, "Audio Un-Muted. Speak Now", Toast.LENGTH_SHORT).show();
                } else {
                    binding.btnMicToggle.setImageResource(R.drawable.mic_off_whitebg);
                    agoraEngine.muteLocalAudioStream(true);
                    agoraEngine.muteRemoteAudioStream(uid, true);

                    if(auth.getUid().equals(callerId)) {
                        database.getReference().child("Users").child(callerId).child("toggleAudio").setValue(true);
                    } else if(auth.getUid().equals(receiverId)) {
                        database.getReference().child("Users").child(receiverId).child("toggleAudio").setValue(true);
                    }
                    Toast.makeText(VideoCallActivity.this, "Audio is Muted. Click Mic to Speak", Toast.LENGTH_SHORT).show();
                }   
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