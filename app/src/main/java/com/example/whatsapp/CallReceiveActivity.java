package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.whatsapp.Models.Users;
import com.example.whatsapp.databinding.ActivityCallReceiveBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Timer;
import java.util.TimerTask;

public class CallReceiveActivity extends AppCompatActivity {

    ActivityCallReceiveBinding binding;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    String callerId,senderId, callerUserName, profileImageURI;
    Users caller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding =ActivityCallReceiveBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        callerId = getIntent().getStringExtra("callerId");
        senderId = auth.getUid();

        database.getReference().child("Users").child(callerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                caller = snapshot.getValue(Users.class);
//                profileImageURI = caller.getProfilepic();
                callerUserName = caller.getUserName();
                profileImageURI = "https://firebasestorage.googleapis.com/v0/b/whatsapp-5a1fe.appspot.com/o/profile_pictures%2FT9H44WOpqXbR4qSmyDS7QJgzWgr1?alt=media&token=df84900b-ca26-4267-9425-566f0c77f879";

                binding.tvCallerName.setText(callerUserName);
                Picasso.get().load(profileImageURI).placeholder(R.drawable.profile).into(binding.profileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("error","Couldn't Fetch Caller Details due to database error");
            }
        });

        binding.btnAcceptCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CallReceiveActivity.this, VideoCallActivity.class);
                database.getReference().child(senderId).child("isAvailableForCalls").setValue(true);
                intent.putExtra("pickerId", senderId);
                startActivity(intent);
            }
        });

        binding.btnRejectCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.getReference().child("Users").child(senderId).child("isAvailableForCalls").setValue(false);
                database.getReference().child("Users").child(senderId).child("incomingVideoCall").setValue("null");
            }
        });
    }
}