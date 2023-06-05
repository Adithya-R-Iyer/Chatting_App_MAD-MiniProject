package com.example.whatsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.whatsapp.databinding.ActivityVideoCallReceiveBinding;

public class VideoCallReceiveActivity extends AppCompatActivity {

    ActivityVideoCallReceiveBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        binding = ActivityVideoCallReceiveBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());


    }

//    private void onCallRequest(String callerId) {
//        if(!callerId.equals("null")) {
//            binding.callLayout.setVisibility(View.VISIBLE);
//            binding.incomingCallText.setText(callerId + " is calling...");
//
//            binding.callAcceptBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    database.getReference().child("Users").child(senderId).child("callConnectionId").setValue(receiverId+senderId);
//                    database.getReference().child("Users").child(senderId).child("isAvailableForCalls").setValue(true);
//
//                    binding.callLayout.setVisibility(View.GONE);
//                    switchToControls();
//                }
//            });
//
//            binding.callRejectBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    database.getReference().child("Users").child(senderId).child("incomingVideoCall").setValue("null");
//                    binding.callLayout.setVisibility(View.GONE);
//                }
//            });
//        }
//    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        binding.webView.loadUrl("about:blank");
        super.onDestroy();
    }
}