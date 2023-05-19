package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsapp.Models.Users;
import com.example.whatsapp.databinding.ActivityChatDetailBinding;
import com.example.whatsapp.databinding.ActivityOtherUserProfileBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

public class OtherUserProfileActivity extends AppCompatActivity {
    ActivityOtherUserProfileBinding binding;
    FirebaseDatabase database;
    TextView username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        binding = ActivityOtherUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database=FirebaseDatabase.getInstance();
//        username=findViewById(R.id.user__Name);
        String receiverId = getIntent().getStringExtra("receiverId");
        String profilepic = getIntent().getStringExtra("profilepic");




        database.getReference().child("Users").child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user=snapshot.getValue(Users.class);
//                Toast.makeText(OtherUserProfileActivity.this, user.getUserName(), Toast.LENGTH_SHORT).show();
                Picasso.get().load(user.getProfilepic()).placeholder(R.drawable.profile).into(binding.receiverProfileImage);
                binding.receiverUserName.setText(user.getUserName());
                binding.receiverEmail.setText(user.getMail());

                if (user.getOnline().equals("online")) {
                    binding.receiverOnlineStatus.setText("online");
                }
                else {
                    // User is offline
                    // Perform the desired action
                    long milli = Long.parseLong(user.getOnline());

                    // Create a new Date object using the timestamp
                    long databaseTimeStamp = milli;
                    Date databaseDate = new Date(databaseTimeStamp);
                    // Create a SimpleDateFormat object to format the date as "HH:mm"
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                    SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy");
                    // Use the SimpleDateFormat object to format the date as a string containing only "HH:mm"
                    String timeString = dateFormat.format(databaseDate);
                    String databaseDateString = dateFormat1.format(databaseDate);

                    //Getting Current Date and Time
                    long currentTimeStamp = System.currentTimeMillis();
                    Date currentDate = new Date(currentTimeStamp);
                    String currentDateString = dateFormat1.format(currentDate);

                    if (currentDateString.equals(databaseDateString)) {
                        binding.receiverOnlineStatus.setText("Last active at "+timeString);
                    } else {
                        binding.receiverOnlineStatus.setText("Last active at "+databaseDateString);
                    }
                }
//                    binding.receiverOnlineStatus.setText(user.getOnline());


                if(user.getStatus()!=null){
                    binding.receiverAbout.setText(user.getStatus());
                }
                else{
                    binding.receiverAbout.setText("this is my about");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OtherUserProfileActivity.this, "some error", Toast.LENGTH_SHORT).show();
            }
        });

        binding.receiverBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1=new Intent(OtherUserProfileActivity.this,ChatDetailActivity.class);
                startActivity(intent1);
            }
        });

    }
}