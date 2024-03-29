package com.example.whatsapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.whatsapp.Adapters.ChatAdapter;
import com.example.whatsapp.Models.MessagesModel;

import com.example.whatsapp.Models.Users;
import com.example.whatsapp.Services.FCMSendMessageService;
import com.example.whatsapp.Services.SecretsManager;
import com.example.whatsapp.databinding.ActivityChatDetailBinding;
//<<<<<<< HEAD
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
//=======
import com.example.whatsapp.databinding.ImagePreviewBottomSheetDialogBinding;
import com.example.whatsapp.databinding.SelectedTypeOfMediaBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
//>>>>>>> 02e71a565f12218fc57641db7746396e40dc7051
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
//<<<<<<< HEAD
import com.google.firebase.messaging.FirebaseMessaging;
//=======
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
//>>>>>>> 02e71a565f12218fc57641db7746396e40dc7051
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class ChatDetailActivity extends AppCompatActivity {

    ActivityChatDetailBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    String scheduled_message="";
    TextView onlineCheck;
    Uri imageUri;

    private Handler handler;
    private Runnable task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //initialise the handler
        handler=new Handler();

        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //  ***********  IV Feature *********
        binding.imageSender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog bottomSheetDialog=new BottomSheetDialog(ChatDetailActivity.this);
                SelectedTypeOfMediaBinding selectedTypeOfMediaBinding= SelectedTypeOfMediaBinding.inflate(getLayoutInflater());
                bottomSheetDialog.setContentView(selectedTypeOfMediaBinding.getRoot());
                bottomSheetDialog.show();

                selectedTypeOfMediaBinding.imageSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent1=new Intent();
                        intent1.setAction(Intent.ACTION_GET_CONTENT);
                        intent1.setType("image/*");

                        startActivityForResult(intent1,45);
                        bottomSheetDialog.dismiss();
                    }
                });

                selectedTypeOfMediaBinding.videoSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent1=new Intent();
                        intent1.setAction(Intent.ACTION_GET_CONTENT);
                        intent1.setType("video/*");

                        startActivityForResult(intent1,45);
                        bottomSheetDialog.dismiss();
                    }
                });

            }
        });
        onlineCheck=binding.onlineCheck;

        //  ***********  IV Feature *********

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

        //Incoming VideoCall Code
        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("incomingVideoCall").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String callerId = snapshot.getValue(String.class);
                if(!Objects.equals(callerId, "null")) {
                    Log.d("vcDebug","onCallRequest method starting...");
                    Intent intent = new Intent(ChatDetailActivity.this, CallReceiveActivity.class);
                    intent.putExtra("callerId", callerId);
                    intent.putExtra("receiverId", auth.getUid());
                    intent.putExtra("srToken", 2);  // Call Receiver
                    intent.putExtra("callType", 2); //Video Call
                    startActivity(intent);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        //Incoming VoiceCall Code
        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("incomingVoiceCall").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String callerId = snapshot.getValue(String.class);
                if(!Objects.equals(callerId, "null")) {
                    Log.d("vcDebug","onCallRequest method starting...");
                    Intent intent = new Intent(ChatDetailActivity.this, CallReceiveActivity.class);
                    intent.putExtra("callerId", callerId);
                    intent.putExtra("receiverId", auth.getUid());
                    intent.putExtra("srToken", 2);  // Call Receiver
                    intent.putExtra("callType", 1); //Voice Call
                    startActivity(intent);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        binding.btnVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatDetailActivity.this, VideoCallActivity.class);
                intent.putExtra("receiverId",receiverId);
                intent.putExtra("callerId", senderId);
                intent.putExtra("srToken",1);  //Sender or caller
                database.getReference().child("Users").child(receiverId).child("incomingVideoCall").setValue(senderId);
                startActivity(intent);
            }
        });

        binding.btnVoiceCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatDetailActivity.this, VoiceCallActivity.class);
                intent.putExtra("receiverId",receiverId);
                intent.putExtra("callerId", senderId);
                intent.putExtra("srToken",1); //  Sender or caller
                database.getReference().child("Users").child(receiverId).child("incomingVoiceCall").setValue(senderId);
                startActivity(intent);
            }
        });

        //HERE SEEING OTHER'S PROFILE CODE

        binding.userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ChatDetailActivity.this,OtherUserProfileActivity.class);
                intent.putExtra("receiverId",receiverId);
                intent.putExtra("profilePic",profilePic);
                intent.putExtra("userName", userName);
                startActivity(intent);
            }
        });


        //HERE ONLINE CHECK
        database.getReference().child("Users").child(receiverId).child("online").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String online = snapshot.getValue(String.class);
                    if (online.equals("online")) {
                        onlineCheck.setText("ONLINE");
                    }
                    else {
                        // User is offline
                        // Perform the desired action
                        long milli=Long.parseLong(online);

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

                        if(currentDateString.equals(databaseDateString))
                        {
                            onlineCheck.setText("Last active at "+timeString);
                        }

                        else{
                            onlineCheck.setText("Last active at "+databaseDateString);
                        }

//                        onlineCheck.setText(online);
//                        Toast.makeText(ChatDetailActivity.this,online,Toast.LENGTH_LONG).show();

                    }
                } else {
                    // Handle the case when the "online" field does not exist for the user
                    Toast.makeText(ChatDetailActivity.this,"sorry online field doesnt exist",Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatDetailActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();

            }
        });


        final ArrayList<MessagesModel> messagesModels = new ArrayList<>();
        //this calls the adapter class which creates styled messages that uses both the activities sample_reciver and sample_sender
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

                //CODE to send notification to the receiver whenever a new message is sent from the sender end
//                database.getReference().child("Users").child(receiverId).child("deviceId").addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        String receiverDeviceId = snapshot.getValue(String.class);
//                        String serverKey = SecretsManager.readSecrets(getApplicationContext(), "firebaseServerKey");
//                        database.getReference().child("Users").child(senderId).child("userName").addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                String senderUserName = snapshot.getValue(String.class);
//                                try {
//                                    FCMSendMessageService.sendMessage(ChatDetailActivity.this, receiverDeviceId, senderUserName, message, serverKey);
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {}
//                        });
//                    }
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {}
//                });

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

        binding.speechToText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SpeakNow(view);
            }
        });

        //this method is used to creating a scheduled message and sending it
        binding.scheduledMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar=Calendar.getInstance();
                int hours=calendar.get(Calendar.HOUR_OF_DAY);
                int minutes=calendar.get(Calendar.MINUTE);

                TimePickerDialog tpd=new TimePickerDialog(ChatDetailActivity.this, androidx.appcompat.R.style.Theme_AppCompat_Dialog, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {

//                        Calendar c=Calendar.getInstance();
//                        c.set(Calendar.HOUR_OF_DAY,hourOfDay);
//                        c.set(Calendar.MINUTE,minute);

                        scheduled_message = binding.etMessage.getText().toString();
                        //if scheduled message is empty then dont send
                        if(scheduled_message.isEmpty()) {
                            binding.etMessage.setError("Message Cannot be Empty");
                            return;
                        }
                        else {
                            binding.etMessage.setText("");
                            scheduleTask(hourOfDay, minute);
                        }
                    }
                },15,00,true);
                tpd.show();

            }
        });

        // Initialize the handler
        handler = new Handler();

        // Initialize the task to change the text
        task = new Runnable() {
            @Override
            public void run() {

//                Toast.makeText(ChatDetailActivity.this, "Text change scheduled", Toast.LENGTH_SHORT).show();
//                    binding.etMessage.setText("scheduled");

//                if(scheduled_message.isEmpty()) {
//                    binding.etMessage.setError("Message Cannot be Empty");
//                    return;
//                }

                String message = scheduled_message;
                final MessagesModel messagesModel = new MessagesModel(senderId, message);
                messagesModel.setTimestamp(new Date().getTime());

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
        };
    }

    private void SpeakNow(View view){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Start Speaking...");
        startActivityForResult(intent,111);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==111 && resultCode== RESULT_OK){
            binding.etMessage.setText(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0));
        }

        //  ***********  IV Feature *********
        if(requestCode==45){
            if(data!=null)
            {
                if (data.getData()!=null){
                    imageUri=data.getData();
                    final BottomSheetDialog bottomSheetDialogImage=new BottomSheetDialog(ChatDetailActivity.this);
                    ImagePreviewBottomSheetDialogBinding previewBottomSheetDialogBinding= ImagePreviewBottomSheetDialogBinding.inflate(getLayoutInflater());
                    bottomSheetDialogImage.setContentView(previewBottomSheetDialogBinding.getRoot());
                    bottomSheetDialogImage.show();

                    String mediaType = getMediaType(imageUri);

                    if(mediaType.equals("image"))
                    {
                        previewBottomSheetDialogBinding.imgMessage.setImageURI(imageUri);
                    }
                    else if(mediaType.equals("video"))
                    {
                        Glide.with(ChatDetailActivity.this).asBitmap().load(imageUri).into(previewBottomSheetDialogBinding.imgMessage);
                    }


//                    Picasso.get().load(imageUri).placeholder(R.drawable.profile).into(previewBottomSheetDialogBinding.imgMessage);
                    Toast.makeText(this, imageUri.toString(), Toast.LENGTH_SHORT).show();

                    previewBottomSheetDialogBinding.sendImg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ProgressDialog progdialog=new ProgressDialog(ChatDetailActivity.this);

                            progdialog.setTitle("Sending Media");
                            progdialog.setMessage("please wait, sending media");

                            if (previewBottomSheetDialogBinding.edtGetMsgDescription.getText().toString().trim().isEmpty()||!previewBottomSheetDialogBinding.edtGetMsgDescription.getText().toString().trim().isEmpty()){
                                progdialog.show();
                                final String senderId = auth.getUid();
                                String receiverId = getIntent().getStringExtra("userId");
                                final String senderRoom = senderId + receiverId; // This ID is used to create a 1st child node inside Chats from Sender to Receiver in the FireBase database
                                final String receiverRoom = receiverId + senderId; // This ID is used to create a  2nd child node inside Chats from Receiver to Sender in the FireBase database

                                final StorageReference storage= FirebaseStorage.getInstance().getReference().child("sent_images").child(senderRoom).child(String.valueOf(new Date().getTime()));
                                storage.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        storage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
//                                                Toast.makeText(ChatDetailActivity.this, "worked", Toast.LENGTH_SHORT).show();
                                                MessagesModel picMsg= new MessagesModel();
                                                picMsg.setuId(senderId);
                                                picMsg.setTimestamp(new Date().getTime());
                                                picMsg.setMedia(uri.toString());
                                                picMsg.setMessageDesc(previewBottomSheetDialogBinding.edtGetMsgDescription.getText().toString());


                                                bottomSheetDialogImage.dismiss();
                                                // Here .push() ensures that a new Id is created with the help of the TimeStamp ...whenever a new message is sent -> Push is usually used to create unique id's
                                                database.getReference().child("chats").child(senderRoom).push().setValue(picMsg).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        //if message from sender is added successfully to the SenderReciver Node then add the same message to the ReceiverSender Node
                                                        database.getReference().child("chats").child(receiverRoom).push().setValue(picMsg).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                progdialog.dismiss();

                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }


    }

    private String getMediaType(Uri imageUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        String extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));

        if (extension != null && extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg") || extension.equalsIgnoreCase("png")) {
            return "image";
        } else if (extension != null && (extension.equalsIgnoreCase("mp4") || extension.equalsIgnoreCase("mov"))) {
            return "video";
        } else {
            return "unknown";
        }
    }

    //  ***********  IV Feature *********

    //method to calculate the delay of after how much time the message should be sent
    private long calculateDelay(int hours,int minutes) {
        // Calculate the delay until the desired time (1:30)
        long currentTimeMillis = System.currentTimeMillis();
        int desiredHour = hours;
        int desiredMinute = minutes;

        // Get the current time components
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTimeMillis);
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        // Calculate the delay until the desired time
        long delay = 0;

        if (currentHour < desiredHour || (currentHour == desiredHour && currentMinute < desiredMinute)) {
            // The desired time is in the future
            calendar.set(Calendar.HOUR_OF_DAY, desiredHour);
            calendar.set(Calendar.MINUTE, desiredMinute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            delay = calendar.getTimeInMillis() - currentTimeMillis;
        }

        return delay;
    }

    //this method schedules whether the task should run or not
    private void scheduleTask(int hours, int minutes) {
        // Get the desired time to change the text
        long delay = calculateDelay(hours,minutes);

        if (delay <= 0) {
            // The desired time has already passed
            Toast.makeText(ChatDetailActivity.this,"time already passed",Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(ChatDetailActivity.this,"scheduled message set to "+String.valueOf(hours)+":"+String.valueOf(minutes),Toast.LENGTH_LONG).show();
        // Post the task with the calculated delay
        handler.postDelayed(task, delay);
    }

    //destroy method
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Remove the task from the handler if the activity is destroyed
        handler.removeCallbacks(task);
    }
}