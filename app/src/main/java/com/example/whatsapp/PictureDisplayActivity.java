package com.example.whatsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.whatsapp.Adapters.ChatAdapter;
import com.squareup.picasso.Picasso;

public class PictureDisplayActivity extends AppCompatActivity {
    ImageView imgview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_display);

        imgview=findViewById(R.id.img_display);

        String imgURI=getIntent().getStringExtra("imgUri");

//        Uri uri= Uri.parse(imgURI);
//
//        imgview.setImageURI(uri);

        Picasso.get().load(imgURI).placeholder(R.drawable.profile).into(imgview);

    }
}