package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.example.whatsapp.Models.Users;
import com.example.whatsapp.databinding.ActivityOtherUserProfileBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class OtherUserProfileActivity extends AppCompatActivity {
    ActivityOtherUserProfileBinding binding;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog progressDialog;

    String something="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        binding = ActivityOtherUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        String receiverId = getIntent().getStringExtra("receiverId");
        String profilePic = getIntent().getStringExtra("profilePic");
        String userName = getIntent().getStringExtra("userName");



        database.getReference().child("Users").child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user=snapshot.getValue(Users.class);
//                Toast.makeText(OtherUserProfileActivity.this, user.getUserName(), Toast.LENGTH_SHORT).show();
                Picasso.get().load(user.getProfilepic()).placeholder(R.drawable.profile).into(binding.receiverProfileImage);
                something=user.getProfilepic();
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
                intent1.putExtra("receiverId",receiverId);
                intent1.putExtra("profilePic",profilePic);
                intent1.putExtra("userName",userName);
                startActivity(intent1);
            }
        });

        binding.downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storage.getReference().child("profile_pictures").child(Objects.requireNonNull(receiverId)).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
//                        progressDialog = new ProgressDialog(OtherUserProfileActivity.this);
//                        progressDialog.setTitle("Download");
//                        progressDialog.setMessage("Downloading Profile Picture...");

                        database.getReference().child("Users").child(receiverId).child("userName").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                progressDialog.show();
                                Toast.makeText(OtherUserProfileActivity.this, "Downloading...", Toast.LENGTH_SHORT).show();
                                // Create a DownloadManager instance
                                DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                // Create a request for the download
                                DownloadManager.Request request = new DownloadManager.Request(uri);
                                // Set the destination path for the downloaded file
                                String fileName = snapshot.getValue(String.class);
                                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName + ".jpg");
                                // Set the notification visibility
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                // Enqueue the download request and get the download ID
                                long downloadId = downloadManager.enqueue(request);
//                                progressDialog.dismiss();

                                Uri downloadedFileUri = downloadManager.getUriForDownloadedFile(downloadId);
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(downloadedFileUri, "image/*");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                startActivity(intent);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });
            }
        });

//        binding.receiverProfileImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(OtherUserProfileActivity.this,something, Toast.LENGTH_SHORT).show();
//            }
//        });
    }
}