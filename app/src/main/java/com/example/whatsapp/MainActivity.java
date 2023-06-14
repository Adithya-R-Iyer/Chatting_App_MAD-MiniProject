package com.example.whatsapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.whatsapp.Adapters.FragmentsAdapter;
import com.example.whatsapp.Models.Users;
import com.example.whatsapp.Services.SecretsManager;
import com.example.whatsapp.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    String senderUid;
    int INTENT_TOKEN = 0; //From signin->MainActivity val=0 , from VideoCall or VoiceCall ->MainActivity val =1
    String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.viewPager.setAdapter(new FragmentsAdapter(getSupportFragmentManager()));
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        senderUid = auth.getUid();
//        setOnlineStatus("online");

        INTENT_TOKEN = getIntent().getIntExtra("intentToken", 0);

        //CODE TO GET THE DEVICE ID - FIREBASE CLOUD MESSAGING
//        FirebaseMessaging.getInstance().getToken()
//                .addOnCompleteListener(new OnCompleteListener<String>() {
//                    @Override
//                    public void onComplete(@NonNull Task<String> task) {
//                        if (!task.isSuccessful()) {
//                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
//                            return;
//                        }
//
//                        // Get new FCM registration token
//                        deviceId = task.getResult();
//                        Log.d("TOKEN ", deviceId);
//                    }
//                });
//        database.getReference().child("Users").child(auth.getUid()).child("deviceId").setValue(deviceId);

        //Incoming VideoCall Code
        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("incomingVideoCall").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String callerId = snapshot.getValue(String.class);
                if(!Objects.equals(callerId, "null")) {
                    Log.d("vcDebug","onCallRequest method starting...");
                    Intent intent = new Intent(MainActivity.this, CallReceiveActivity.class);
                    intent.putExtra("callerId", callerId);
                    intent.putExtra("receiverId", auth.getUid());
                    intent.putExtra("srToken", 2); // Call Receiver
                    intent.putExtra("callType", 2); // Video Call
                    startActivity(intent);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        //Listening to Incoming Voice Call
        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("incomingVoiceCall").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String callerId = snapshot.getValue(String.class);
                if(!Objects.equals(callerId, "null")) {
                    Log.d("vcDebug","onCallRequest method starting...");
                    Intent intent = new Intent(MainActivity.this, CallReceiveActivity.class);
                    intent.putExtra("callerId", callerId);
                    intent.putExtra("receiverId", auth.getUid());
                    intent.putExtra("srToken", 2); // Call Receiver
                    intent.putExtra("callType", 1); // Voice Call
                    startActivity(intent);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.setting:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;

            case R.id.groupChat:
                Intent intent1 = new Intent(MainActivity.this, GroupChatActivity.class);
                startActivity(intent1);
                break;

            case R.id.chatBot:
                Intent intent3 = new Intent(MainActivity.this, ChatBotActivity.class);
                startActivity(intent3);
                break;

            case R.id.logout:

                database.getReference().child("Users").child(senderUid).child("online").setValue(String.valueOf(new Date().getTime()));
                Log.d("onlineDebug","logout time set ");
                auth.signOut();
                Intent intent2 = new Intent(MainActivity.this, SignInActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent2);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //what happens when mobile back button is pressed
    @Override
    public void onBackPressed() {
        // Close the app completely
            database.getReference().child("Users").child(senderUid).child("online").setValue(String.valueOf(new Date().getTime()));
            finish();
//        System.exit(0);
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
// NOTE TO DEVELOPERS :- DON'T USE THE ONDESTROY METHOD TO SET LOGOUT TIME to `online` property of the user...
//    }
}