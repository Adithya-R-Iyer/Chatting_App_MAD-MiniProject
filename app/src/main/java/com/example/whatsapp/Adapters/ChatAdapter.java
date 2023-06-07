package com.example.whatsapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsapp.Models.MessagesModel;
import com.example.whatsapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

public class ChatAdapter extends RecyclerView.Adapter{

    ArrayList<MessagesModel> messagesModels;
    Context context;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    String recvId;

    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;
    
    int flag =0;

    public ChatAdapter(ArrayList<MessagesModel> messagesModels, Context context) {
        this.messagesModels = messagesModels;
        this.context = context;
    }

    public ChatAdapter(ArrayList<MessagesModel> messagesModels, Context context, String recvId) {
        this.messagesModels = messagesModels;
        this.context = context;
        this.recvId = recvId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == SENDER_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_sender, parent, false);
            return new SenderViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_receiver, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MessagesModel messagesModel = messagesModels.get(position);

        //if the position of the array list is greater than or equal to 1 then ...get the details(date) of the previous message and check if both the messages are from the same day... if so return 1 else return 0
        if(position>=1){
            MessagesModel prevMessageModel = messagesModels.get(position-1);

            Date thisDate = new Date(messagesModel.getTimestamp());
            Date prevDate = new Date(prevMessageModel.getTimestamp());
            SimpleDateFormat dateFormat0 = new SimpleDateFormat("dd/MM/yyyy");
            String thisDateString = dateFormat0.format(thisDate);
            String prevDateString = dateFormat0.format(prevDate);

            if(thisDateString.equals(prevDateString)){
                flag=1; // true
            }
            else {
                flag=0; //false
            }
        }

        // Code To delete a message when user long clicks on any message
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                new AlertDialog.Builder(context)
                        .setTitle("Delete")
                        .setMessage("Are you sure you want to delete this message")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                FirebaseAuth auth = FirebaseAuth.getInstance();
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                String senderRoom = auth.getUid() + recvId;
                                database.getReference().child("chats").child(senderRoom).child(messagesModel.getMessageId()).setValue(null);

                            }
                        })
                        .setNegativeButton("no", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();

                return false;
            }
        });

        //CONVERSION OF TIMESTAMP TO DATE :- HH:mm
        // Create a new Date object using the timestamp
        long databaseTimeStamp = messagesModel.getTimestamp();
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

//        Instant timestamp1 = null;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            timestamp1 = Instant.ofEpochMilli(databaseTimeStamp);
//        }
//        Instant timestamp2 = null;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            timestamp2 = Instant.ofEpochMilli(currentTimeStamp);
//        }
//
//        LocalDate date1 = null;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            date1 = timestamp1.atZone(ZoneId.systemDefault()).toLocalDate();
//        }
//        LocalDate date2 = null;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            date2 = timestamp2.atZone(ZoneId.systemDefault()).toLocalDate();
//        }

        if(holder.getClass() == SenderViewHolder.class) {
            //Conditional Statement to Verify and display the dates accordingly
            if(currentDateString.equals(databaseDateString) && flag==0){
                ((SenderViewHolder)holder).etDate.setText("Today");
                ((SenderViewHolder)holder).etDate.setVisibility(View.VISIBLE);
            }
            else if(flag==1) {
                ((SenderViewHolder)holder).etDate.setVisibility(View.GONE);
                flag=0;
            }
            else if((!currentDateString.equals(databaseDateString)) && (flag==0)) {
                ((SenderViewHolder)holder).etDate.setText(databaseDateString);
                ((SenderViewHolder)holder).etDate.setVisibility(View.VISIBLE);
            }

            ((SenderViewHolder)holder).senderText.setText(messagesModel.getMessage());
            ((SenderViewHolder)holder).senderTime.setText(timeString);
        }
        else {
            //Conditional Statements to verify and display the dates accordingly
            if(currentDateString.equals(databaseDateString) && flag==0){
                ((ReceiverViewHolder)holder).etDate.setText("Today");
                ((ReceiverViewHolder)holder).etDate.setVisibility(View.VISIBLE);
            }
            else if(flag==1) {
                ((ReceiverViewHolder)holder).etDate.setVisibility(View.GONE);
                flag=0;
            }
            else if((!currentDateString.equals(databaseDateString)) && (flag==0)) {
                ((ReceiverViewHolder)holder).etDate.setText(databaseDateString);
                ((ReceiverViewHolder)holder).etDate.setVisibility(View.VISIBLE);
            }

            ((ReceiverViewHolder)holder).receiverText.setText(messagesModel.getMessage());
            ((ReceiverViewHolder)holder).receiverTime.setText(timeString);
        }

    }

//    The getItemViewType() function is called by the RecyclerView to get the view type of the item at the given position.
//    It is called when the RecyclerView is created, or when the data set of the adapter is changed
    //first function that will be called automatically
    @Override
    public int getItemViewType(int position) {

        if(messagesModels.get(position).getuId().equals(auth.getUid())) {
            return SENDER_VIEW_TYPE;
        }
        else {
            return  RECEIVER_VIEW_TYPE;
        }
//        The returned value is used by the adapter's onCreateViewHolder() function to inflate the corresponding layout file for the given view type
//        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return messagesModels.size();
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {

        TextView receiverText, receiverTime, etDate;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverText = itemView.findViewById(R.id.receiverText);
            receiverTime = itemView.findViewById(R.id.receiverTime);
            etDate = itemView.findViewById(R.id.etDate);

        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {

        TextView senderText, senderTime, etDate;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderText = itemView.findViewById(R.id.senderText);
            senderTime = itemView.findViewById(R.id.senderTime);
            etDate = itemView.findViewById(R.id.etDate);
        }
    }

}

