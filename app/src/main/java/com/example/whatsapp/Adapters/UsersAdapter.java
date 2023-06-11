package com.example.whatsapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsapp.ChatDetailActivity;
import com.example.whatsapp.Models.Users;
import com.example.whatsapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder>{

    ArrayList<Users> list;
    Context context;

    public UsersAdapter(ArrayList<Users> list, Context context) {
        this.list = list;
        this.context = context;
    }

    //Where to get the Single Card as ViewHolder object
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_show_user, parent, false);
        return new ViewHolder(view);
    }

    //What will happen after we create the ViewHolder Object
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users user = list.get(position);
        Picasso.get().load(user.getProfilepic()).placeholder(R.drawable.profile).into(holder.image);
        holder.userName.setText(user.getUserName());

        //Code to display User's Last Message on their profile
        FirebaseDatabase.getInstance().getReference().child("chats")
                .child(FirebaseAuth.getInstance().getUid() + user.getUserId())
                .orderByChild("timestamp")
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChildren()) {
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                holder.lastMessage.setText(dataSnapshot.child("message").getValue().toString());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        
                    }
                });

        //Code to display User's Last Message Time on their profile
        FirebaseDatabase.getInstance().getReference().child("chats")
                .child(FirebaseAuth.getInstance().getUid() + user.getUserId())
                .orderByChild("timestamp")
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChildren()) {
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()) {

                                //CONVERSION OF TIMESTAMP TO DATE :- HH:mm
                                // Create a new Date object using the timestamp
                                long lastMessageTimeStamp = (long) dataSnapshot.child("timestamp").getValue();
                                Date lastMessageDate = new Date(lastMessageTimeStamp);
                                // Create a SimpleDateFormat object to format the date as "HH:mm"
                                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                                SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy");
                                // Use the SimpleDateFormat object to format the date as a string containing only "HH:mm"
                                String lastMessageTimeString = dateFormat.format(lastMessageDate);
                                String lastMessageDateString = dateFormat1.format(lastMessageDate);

                                //Getting Current Date and Time
                                long currentTimeStamp = System.currentTimeMillis();
                                Date currentDate = new Date(currentTimeStamp);
                                String currentDateString = dateFormat1.format(currentDate);

                                if(currentDateString.equals(lastMessageDateString)) {
                                    holder.lastMessageTime.setText(lastMessageTimeString);
                                }
                                else if(!currentDateString.equals(lastMessageDateString)) {
                                    holder.lastMessageTime.setText(lastMessageDateString);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        // Code to send Data from ChatsFragments Activity to the ChatDetail Activity when clicked on a User's Profle
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatDetailActivity.class);
                intent.putExtra("userId",user.getUserId());
                intent.putExtra("profilePic",user.getProfilepic());
                intent.putExtra("userName",user.getUserName());
                context.startActivity(intent);
            }
        });
    }

    //How Many Items ??
    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView userName, lastMessage, lastMessageTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.profileImage);
            userName = itemView.findViewById(R.id.userNameList);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            lastMessageTime = itemView.findViewById(R.id.lastMessageTime);

        }
    }

}
