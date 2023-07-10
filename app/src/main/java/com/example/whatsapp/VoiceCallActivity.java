package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.Manifest;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.ChannelMediaOptions;

import com.example.whatsapp.Models.Users;
import com.example.whatsapp.Services.SecretsManager;
import com.example.whatsapp.Services.TokenGenerator;
import com.example.whatsapp.databinding.ActivityVoiceCallBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class VoiceCallActivity extends AppCompatActivity {


    ActivityVoiceCallBinding binding;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    // Fill the App ID of your project generated on Agora Console.
    private String appId = "";
    // Fill the channel name.
    private String channelName = "";
    // Fill the temp token generated on Agora Console.
    private String token = "";
    // An integer that identifies the local user.
    private int uid = 0;
    // Track the status of your connection
    private boolean isJoined = false;

    // Agora engine instance
    private RtcEngine agoraEngine;

    int SR_TOKEN;  // caller - 1   receiver - 2
    int EXECUTION_TOKEN = 1; //RUN = 1  & STOP = 0

    String callerId="", receiverId="";
    Boolean isAudio = true;

    Handler handler = new Handler();
    String callDuration = "00:00:01";

    // Parse the time string into a LocalTime object
    LocalTime callDurationTime = LocalTime.parse(callDuration, DateTimeFormatter.ofPattern("HH:mm:ss"));

    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS =
            {
                    Manifest.permission.RECORD_AUDIO
            };

    private boolean checkSelfPermission()
    {
        return ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED;
    }

    void showMessage(String message) {
        runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }

    private void setupVoiceSDKEngine() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = appId;
            config.mEventHandler = mRtcEventHandler;
            agoraEngine = RtcEngine.create(config);
        } catch (Exception e) {
            throw new RuntimeException("Check the error.");
        }
    }

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        // Listen for the remote user joining the channel.
        public void onUserJoined(int uid, int elapsed) {
            runOnUiThread(()->Toast.makeText(VoiceCallActivity.this, "Remote user joined: " + uid, Toast.LENGTH_SHORT).show());
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            // Successfully joined a channel
            isJoined = true;
            showMessage("Joined Channel " + channel);
            runOnUiThread(()->Toast.makeText(VoiceCallActivity.this, "Waiting for a remote user to join", Toast.LENGTH_SHORT).show());
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            // Listen for remote users leaving the channel
            showMessage("Remote user offline " + uid + " " + reason);
            if (isJoined) runOnUiThread(()->Toast.makeText(VoiceCallActivity.this, "Waiting for a remote user to join", Toast.LENGTH_SHORT).show());
        }

        @Override
        public void onLeaveChannel(RtcStats 	stats) {
            // Listen for the local user leaving the channel
            runOnUiThread(()->Toast.makeText(VoiceCallActivity.this, "Press the button to join a channel", Toast.LENGTH_SHORT).show());
            isJoined = false;
        }
    };

    private void joinChannel() {
        ChannelMediaOptions options = new ChannelMediaOptions();
        options.autoSubscribeAudio = true;
        // Set both clients as the BROADCASTER.
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        // Set the channel profile as BROADCASTING.
        options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;

        // Join the channel with a temp token.
        // You need to specify the user ID yourself, and ensure that it is unique in the channel.
        agoraEngine.joinChannel(token, channelName, uid, options);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityVoiceCallBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        receiverId = getIntent().getStringExtra("receiverId");
        callerId = getIntent().getStringExtra("callerId");
        SR_TOKEN =  getIntent().getIntExtra("srToken", 0 );

        appId = SecretsManager.readSecrets(getApplicationContext(), "agoraAppId");
        String agoraAppCertificate = SecretsManager.readSecrets(getApplicationContext(), "agoraAppCertificate");

        //Creating a unique channel name and generating a token - valid for an hour
        channelName = callerId+receiverId;

        try {
            token = TokenGenerator.generateToken(channelName, appId, agoraAppCertificate);
        } catch (Exception e) {
            Toast.makeText(this, "Token Generation Failed. Exiting", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(VoiceCallActivity.this, MainActivity.class);
            intent.putExtra("intentToken", 1);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            database.getReference().child("Users").child(receiverId).child("incomingVoiceCall").setValue("null");
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

        //Code to change the CallDuration Dynamically
        if(callerId.equals(auth.getUid()))
            binding.tvCallDuration.setText("Ringing...");

        database.getReference().child("Users").child(receiverId).child("isAvailableForCalls").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isAvailableForCalls = snapshot.getValue(Boolean.class);
                if(Boolean.TRUE.equals(isAvailableForCalls)) {
                    handler.postDelayed(updateTimerTask,0);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // If all the permissions are granted, initialize the RtcEngine object and join a channel.
        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        }

        setupVoiceSDKEngine();
        joinChannel();

        binding.btnEndCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                agoraEngine.leaveChannel();
                handler.removeCallbacks(updateTimerTask); // remove callBacks that updates the callDuration Time
                Intent intent = new Intent(VoiceCallActivity.this, MainActivity.class);
                intent.putExtra("intentToken", 1);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                database.getReference().child("Users").child(receiverId).child("incomingVoiceCall").setValue("null");
                database.getReference().child("Users").child(receiverId).child("isAvailableForCalls").setValue(false);
                database.getReference().child("Users").child(callerId).child("toggleAudio").setValue(false);
                database.getReference().child("Users").child(receiverId).child("toggleAudio").setValue(false);
                if(SR_TOKEN==1)
                    EXECUTION_TOKEN = 0; // STOP Runnable
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "Voice Call Ended", Toast.LENGTH_SHORT).show();
                finish();
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
                    Toast.makeText(VoiceCallActivity.this, "Audio Un-Muted. Speak Now", Toast.LENGTH_SHORT).show();
                } else {
                    binding.btnMicToggle.setImageResource(R.drawable.mic_off_whitebg);
                    agoraEngine.muteLocalAudioStream(true);
                    agoraEngine.muteRemoteAudioStream(uid, true);

                    if(auth.getUid().equals(callerId)) {
                        database.getReference().child("Users").child(callerId).child("toggleAudio").setValue(true);
                    } else if(auth.getUid().equals(receiverId)) {
                        database.getReference().child("Users").child(receiverId).child("toggleAudio").setValue(true);
                    }
                    Toast.makeText(VoiceCallActivity.this, "Audio is Muted. Click Mic to Speak", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //code to check if the receive rejected the call... if so ...terminate the user and send him back to main-activity within a few seconds
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
                            String incomingCall = user.getIncomingVoiceCall();
                            if (incomingCall.equals("null")) {
                                agoraEngine.leaveChannel();
                                handler.removeCallbacks(updateTimerTask); // remove callBacks that updates the callDuration Time
                                Intent intent = new Intent(VoiceCallActivity.this, MainActivity.class);
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
//                                receiverAvailableForCalls = true;
                                Toast.makeText(getApplicationContext(), "Call in Progress...", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }
        },0, 5000);
//    }
    }

    private final Runnable updateTimerTask = new Runnable() {
        @Override
        public void run() {
            binding.tvCallDuration.setText("" + callDuration);
            //Wait for one second
            handler.postDelayed(this, 1000);
            // Increment the time by one second
            callDurationTime = callDurationTime.plusSeconds(1);
            // Format the updated time back to a string
            callDuration = callDurationTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
    };

    protected void onDestroy() {
        super.onDestroy();
        agoraEngine.leaveChannel();

        // Destroy the engine in a sub-thread to avoid congestion
        new Thread(() -> {
            RtcEngine.destroy();
            agoraEngine = null;
        }).start();
    }

}