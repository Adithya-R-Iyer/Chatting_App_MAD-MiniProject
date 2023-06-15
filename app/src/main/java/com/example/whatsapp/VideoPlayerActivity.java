package com.example.whatsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoPlayerActivity extends AppCompatActivity {

    VideoView vid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        MediaController mdc=new MediaController(this);

        vid=findViewById(R.id.vid);
        vid.setMediaController(mdc);

        mdc.setAnchorView(vid);

        String vidURI=getIntent().getStringExtra("vidUri");

        Uri uri= Uri.parse(vidURI);

        vid.setVideoURI(uri);

        vid.start();


    }
}