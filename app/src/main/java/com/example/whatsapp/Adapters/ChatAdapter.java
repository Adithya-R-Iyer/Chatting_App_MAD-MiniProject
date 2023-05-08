package com.example.whatsapp.Adapters;

import android.content.Context;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsapp.Models.MessagesModel;
import com.example.whatsapp.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter{

    ArrayList<MessagesModel> messagesModels;
    Context context;
    FirebaseAuth auth = FirebaseAuth.getInstance();

    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;

    public ChatAdapter(ArrayList<MessagesModel> messagesModels, Context context) {
        this.messagesModels = messagesModels;
        this.context = context;
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

        if(holder.getClass() == SenderViewHolder.class) {
            ((SenderViewHolder)holder).senderText.setText(messagesModel.getMessage());
        }
        else {
            ((ReceiverViewHolder)holder).receiverText.setText(messagesModel.getMessage());
        }

    }

//    The getItemViewType() function is called by the RecyclerView to get the view type of the item at the given position.
//    It is called when the RecyclerView is created, or when the data set of the adapter is changed
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

        TextView receiverText, receiverTime;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverText = itemView.findViewById(R.id.receiverText);
            receiverTime = itemView.findViewById(R.id.receiverTime);

        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {

        TextView senderText, senderTime;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderText = itemView.findViewById(R.id.senderText);
            senderTime = itemView.findViewById(R.id.senderTime);
        }
    }

}

