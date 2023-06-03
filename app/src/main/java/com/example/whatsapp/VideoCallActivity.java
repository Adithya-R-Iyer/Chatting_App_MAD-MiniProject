package com.example.whatsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.whatsapp.databinding.ActivityVideoCallBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class VideoCallActivity extends AppCompatActivity {

    ActivityVideoCallBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    String senderId= "";
    String receiverId = "";
    String userName = "";
    String profilePic = "";

    Boolean isPeerConnected = false;
    Boolean isAudio = true;
    Boolean isVideo = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        binding = ActivityVideoCallBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        senderId = auth.getUid();
        receiverId = getIntent().getStringExtra("receiverId");
        userName = getIntent().getStringExtra("userName");
        profilePic = getIntent().getStringExtra("profilePic");

    }
}