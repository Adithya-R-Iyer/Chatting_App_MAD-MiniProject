package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;

import com.example.whatsapp.Adapters.ChatAdapter;
import com.example.whatsapp.Models.MessagesModel;
import com.example.whatsapp.databinding.ActivityChatDetailBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class ChatDetailActivity extends AppCompatActivity {

    ActivityChatDetailBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        final String senderId = auth.getUid();
        String receiverId = getIntent().getStringExtra("userId");
        String userName = getIntent().getStringExtra("userName");
        String profilePic = getIntent().getStringExtra("profilePic");

        binding.userName.setText(userName);
        Picasso.get().load(profilePic).placeholder(R.drawable.profile).into(binding.profileImage);

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatDetailActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        final ArrayList<MessagesModel> messagesModels = new ArrayList<>();
        final ChatAdapter adapter = new ChatAdapter(messagesModels, this, receiverId);
        binding.chatRecyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        final String senderRoom = senderId + receiverId; // This ID is used to create a 1st child node inside Chats from Sender to Receiver in the FireBase database
        final String receiverRoom = receiverId + senderId; // This ID is used to create a  2nd child node inside Chats from Receiver to Sender in the FireBase database

        // Code to update the RecyclerView Whenever a new Chat is send by the sender
        database.getReference().child("chats").child(senderRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesModels.clear();
                for(DataSnapshot snapshot1 : snapshot.getChildren()) {
                    MessagesModel model = snapshot1.getValue(MessagesModel.class); // By passing MessageModel.class as an parameter we are telling Firebase Database to deserialize the data snapshot into an object of type MessagesModel
                    model.setMessageId(snapshot1.getKey());
                    messagesModels.add(model);
                }
                adapter.notifyDataSetChanged(); //not compulsory as the RecyclerView can sometimes automatically detect changes by itself when a new data is appended to the end of the list without using this method
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(binding.etMessage.getText().toString().isEmpty()) {
                    binding.etMessage.setError("Message Cannot be Empty");
                    return;
                }

                String message = binding.etMessage.getText().toString();
                final MessagesModel messagesModel = new MessagesModel(senderId, message);
                messagesModel.setTimestamp(new Date().getTime());
                binding.etMessage.setText("");

                // Here .push() ensures that a new Id is created with the help of the TimeStamp ...whenever a new message is sent -> Push is usually used to create unique id's
                database.getReference().child("chats").child(senderRoom).push().setValue(messagesModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //if message from sender is added successfully to the SenderReciver Node then add the same message to the ReceiverSender Node
                        database.getReference().child("chats").child(receiverRoom).push().setValue(messagesModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                            }
                        });
                    }
                });

            }
        });

    }
}