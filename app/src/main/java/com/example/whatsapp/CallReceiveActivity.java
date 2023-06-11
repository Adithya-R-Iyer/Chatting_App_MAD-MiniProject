package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
    String callerId,receiverId,callerUserName, profileImageURI, senderId;
    int SR_TOKEN, EXECUTION_TOKEN=1; // RUN - 1  , STOP =0

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding =ActivityCallReceiveBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        callerId = getIntent().getStringExtra("callerId");
        receiverId = getIntent().getStringExtra("receiverId");
        SR_TOKEN = getIntent().getIntExtra("srToken", 0);
        senderId = auth.getUid();  // note that here at this point senderId is equal to call receiver's receiverId

        database.getReference().child("Users").child(callerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users caller = snapshot.getValue(Users.class);
                assert caller != null;
                profileImageURI = caller.getProfilepic();
                callerUserName = caller.getUserName();
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
                database.getReference().child("Users").child(receiverId).child("isAvailableForCalls").setValue(true);
                intent.putExtra("callerId", callerId);
                intent.putExtra("receiverId", receiverId);
                intent.putExtra("srToken",2);
                EXECUTION_TOKEN=0;
                startActivity(intent);
                finish();
            }
        });

        binding.btnRejectCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CallReceiveActivity.this, MainActivity.class);
                intent.putExtra("intentToken", 1);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                database.getReference().child("Users").child(receiverId).child("isAvailableForCalls").setValue(false);
                database.getReference().child("Users").child(receiverId).child("incomingVideoCall").setValue("null");
                EXECUTION_TOKEN = 0;
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "Video Chat Ended", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        //Code to perform the task ...if caller ends the call before the receiver responds to it
        Timer timer = new Timer();
        // Create a TimerTask that defines the task to be executed
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // Code to be executed after the specified time
                if(EXECUTION_TOKEN==1) {
                    database.getReference().child("Users").child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Users user = snapshot.getValue(Users.class);
                            assert user != null;
                            Boolean isAvailableForCalls = user.getAvailableForCalls();
                            String incomingVideoCall = user.getIncomingVideoCall();
                            if(Boolean.FALSE.equals(isAvailableForCalls) && incomingVideoCall.equals("null")) {
                                Intent intent = new Intent(CallReceiveActivity.this, MainActivity.class);
                                intent.putExtra("intentToken", 1);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(CallReceiveActivity.this, "Please Respond to the Incoming Call...", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }

                    });
                }
            }
        };
        // Schedule the task to be executed after 5 seconds
        timer.schedule(task, 15000);
    }
}