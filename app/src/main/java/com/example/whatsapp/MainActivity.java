package com.example.whatsapp;

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
import com.example.whatsapp.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    String senderUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.viewPager.setAdapter(new FragmentsAdapter(getSupportFragmentManager()));
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        senderUid = auth.getUid();
//        setOnlineStatus("online");
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

            case R.id.logout:

//                database.getReference().child("Users").child(senderUid).child("online").setValue(String.valueOf(new Date().getTime()));
                auth.signOut();
                Intent intent2 = new Intent(MainActivity.this, SignInActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent2);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

//    private void setOnlineStatus(String online){
//        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users").child(auth.getUid());
//        HashMap<String,Object> hashMap=new HashMap<>();
//        Users user=new Users();
//        hashMap.put("online",online);
//        ref.updateChildren(hashMap);
//    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        setOnlineStatus("online");
//    }
//
//
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        setOnlineStatus("offline");
//    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.getReference().child("Users").child(senderUid).child("online").setValue(String.valueOf(new Date().getTime()));
    }
}