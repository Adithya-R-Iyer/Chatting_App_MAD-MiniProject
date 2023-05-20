package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.whatsapp.API_Models.BrainShopMsgModel;
import com.example.whatsapp.APIs.RetrofitAPI;
import com.example.whatsapp.Adapters.ChatBotAdapter;
import com.example.whatsapp.Models.ChatBotMessageModel;
import com.example.whatsapp.databinding.ActivityChatBotBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatBotActivity extends AppCompatActivity {

    ActivityChatBotBinding binding;
    private Handler handler;
    private Runnable task;
    String scheduled_message="";
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    final String chatBotUid = "Bot@01";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        binding = ActivityChatBotBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatBotActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        final ArrayList<ChatBotMessageModel> messagesModels = new ArrayList<>();
//        final ArrayList<String> userNames = new ArrayList<>();

        final String senderId = FirebaseAuth.getInstance().getUid();
//        final String chatBotUid = "Bot@01";
        binding.userName.setText("My AI");

        final ChatBotAdapter adapter = new ChatBotAdapter(messagesModels, this);
        binding.chatRecyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);


        database.getReference().child("ChatBot").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesModels.clear();

                for(DataSnapshot dataSnapshot : snapshot.getChildren() ) {
                    ChatBotMessageModel model = dataSnapshot.getValue(ChatBotMessageModel.class);
                    model.setMessageId(dataSnapshot.getKey());

//                    database.getReference().child("Users").child(model.getuId()).child("userName").addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            String username = snapshot.getValue(String.class);
////                            userNames.add(username);
//                            model.setUserName(username);
////                            Log.d("msg","entered" + model.getUserName());
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });

                    messagesModels.add(model);
                }
//                Log.d("userNames", messagesModels.get(1));
                adapter.notifyDataSetChanged();
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

                final String message = binding.etMessage.getText().toString();

                final ChatBotMessageModel model = new ChatBotMessageModel(senderId, message);
                model.setTimestamp(new Date().getTime());

                binding.etMessage.setText("");

                database.getReference().child("ChatBot").push().setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                });

                //Code to send User's message to chatbot and receive reply from chatbot
                getResponse(message);

            }
        });

        binding.speechToText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SpeakNow(view);
            }
        });

        binding.scheduledMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar=Calendar.getInstance();
                int hours=calendar.get(Calendar.HOUR_OF_DAY);
                int minutes=calendar.get(Calendar.MINUTE);

                TimePickerDialog tpd=new TimePickerDialog(ChatBotActivity.this, androidx.appcompat.R.style.Theme_AppCompat_Dialog, new TimePickerDialog.OnTimeSetListener() {
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

                final ChatBotMessageModel messagesModel = new ChatBotMessageModel(senderId ,message);
                messagesModel.setTimestamp(new Date().getTime());


                // Here .push() ensures that a new Id is created with the help of the TimeStamp ...whenever a new message is sent -> Push is usually used to create unique id's
                database.getReference().child("ChatBot").push().setValue(messagesModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

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
    }

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
            Toast.makeText(ChatBotActivity.this,"time already passed",Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(ChatBotActivity.this,"scheduled message set to "+String.valueOf(hours)+":"+String.valueOf(minutes),Toast.LENGTH_LONG).show();
        // Post the task with the calculated delay
        handler.postDelayed(task, delay);
    }

    //Method to get response from the ChatBot
    private void getResponse(String message) {
        String url = "http://api.brainshop.ai/get?bid=175414&key=sgRtUAfnFLrOcMq5&uid=[uid]&msg=["+ message + "]";
        String BASE_URL = "http://api.brainshop.ai/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        Call<BrainShopMsgModel> call = retrofitAPI.getMessage(url);
        call.enqueue(new Callback<BrainShopMsgModel>() {
            @Override
            public void onResponse(Call<BrainShopMsgModel> call, Response<BrainShopMsgModel> response) {
                if(response.isSuccessful()){
                    BrainShopMsgModel brainShopMsgModel = response.body();
                    ChatBotMessageModel model1 = new ChatBotMessageModel(chatBotUid, brainShopMsgModel.getCnt());
                    model1.setTimestamp(new Date().getTime());

                    database.getReference().child("ChatBot").push().setValue(model1).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<BrainShopMsgModel> call, Throwable t) {

                ChatBotMessageModel model2 = new ChatBotMessageModel(chatBotUid, "Please Revert Your Question");
                model2.setTimestamp(new Date().getTime());

                database.getReference().child("ChatBot").push().setValue(model2).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                });
            }
        });
    }

    //destroy method
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the task from the handler if the activity is destroyed
        handler.removeCallbacks(task);
    }
}