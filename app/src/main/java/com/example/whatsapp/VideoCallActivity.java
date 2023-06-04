package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.whatsapp.APIs.JavaScriptInterface;
import com.example.whatsapp.databinding.ActivityVideoCallBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class VideoCallActivity extends AppCompatActivity {

    ActivityVideoCallBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    String senderId= "";
    String receiverId = "";
    String userName = "";
    String profilePic = "";

    Boolean isPeerConnected = false;
    Boolean isAudio = true;
    Boolean isVideo = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        binding = ActivityVideoCallBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        senderId = auth.getUid();
        receiverId = getIntent().getStringExtra("receiverId");
        userName = getIntent().getStringExtra("userName");
        profilePic = getIntent().getStringExtra("profilePic");

        binding.btnCallEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.getReference().child("Users").child(senderId).child("callConnectionId").setValue("null");
                database.getReference().child("Users").child(receiverId).child("callConnectionId").setValue("null");
                database.getReference().child("Users").child(receiverId).child("isAvailableForCalls").setValue(false);
            }
        });

        binding.btnCameraOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isVideo = !isVideo;
                callJavaScriptFunction("toggleVideo('" + isVideo + "');");
                if (isVideo) {
                    binding.btnCameraOff.setImageResource(R.drawable.camera_off_whitebg);
                } else {
                    binding.btnCameraOff.setImageResource(R.drawable.camera_whitebg);
                }
            }
        });

        binding.btnMicOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAudio = !isAudio;
                callJavaScriptFunction("toggleAudio('" + isAudio + "');");
                if (isAudio) {
                    binding.btnMicOff.setImageResource(R.drawable.mic_off_whitebg);
                } else {
                    binding.btnMicOff.setImageResource(R.drawable.mic_on_whitebg);
                }
            }
        });

        setUpWebView();
        makeCall();
    }

    private void makeCall() {

        if(isPeerConnected){
            database.getReference().child("Users").child(receiverId).child("incomingVideoCall").setValue(auth.getUid()); //Notify Receiver Who the Caller Is??
            database.getReference().child("Users").child(receiverId).child("isAvailableForCalls").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.getValue(Boolean.class).toString().equals("true")){
                        database.getReference().child("Users").child(senderId).child("callConnectionId").setValue(senderId+receiverId);
                        switchToControls();
                        callJavaScriptFunction("startCall('" + receiverId + "');");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else{
            Toast.makeText(this, "You're not connected. Check your internet", Toast.LENGTH_LONG).show();
        }
    }

    private void setUpWebView() {

        binding.webView.setWebChromeClient(new CustomWebChromeClient());
        // Enable JavaScript in WebView
        binding.webView.getSettings().setJavaScriptEnabled(true);   //Warning says that there may be vulnerabilities in the js file... since we have coded the web content ourselves... we can choose to ignore this warning
        binding.webView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        // Create a JavaScript interface object
        JavaScriptInterface jsInterface = new JavaScriptInterface();
        // Add the JavaScript interface to the WebView
        binding.webView.addJavascriptInterface(jsInterface, "AndroidInterface");

        loadVideoCall();
    }

    public void onPeerConnected() {
        isPeerConnected = true;
    }

    private void loadVideoCall() {

        String filePath = "call.html";
        // Get the AssetManager
        AssetManager assetManager = getAssets();

        try {
            // Open the file using the AssetManager
            InputStream inputStream = assetManager.open(filePath);
            // Convert the InputStream to a file path
            File file = convertInputStreamToFile(inputStream);
            // Get the absolute file path
            String absoluteFilePath = file.getAbsolutePath();
            // Load the file in the WebView
            binding.webView.loadUrl("file://" + absoluteFilePath);

            binding.webView.setWebViewClient(new WebViewClient(){
                @Override
                public void onPageFinished(WebView view, String url) {
                    Log.d("vcDebug","OnPageFinished Called");
                    super.onPageFinished(view, url);
                    // This method will be called when the web page has completely loaded
                    initializePeer();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializePeer() {

            callJavaScriptFunction("init('" + senderId + "');");
            Log.d("vcDebug","JavaScript init function called");
            onPeerConnected();
            database.getReference().child("Users").child(senderId).child("incomingVideoCall").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    onCallRequest(snapshot.getValue(String.class));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
    }

    private void onCallRequest(String callerId) {
        if(!callerId.equals("null")) {
            binding.callLayout.setVisibility(View.VISIBLE);
            binding.incomingCallText.setText(callerId + " is calling...");

            binding.callAcceptBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    database.getReference().child("Users").child(senderId).child("callConnectionId").setValue(receiverId+senderId);
                    database.getReference().child("Users").child(senderId).child("isAvailableForCalls").setValue(true);

                    binding.callLayout.setVisibility(View.GONE);
                    switchToControls();
                }
            });

            binding.callRejectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    database.getReference().child("Users").child(senderId).child("incomingVideoCall").setValue("null");
                    binding.callLayout.setVisibility(View.GONE);
                }
            });
        }
    }

    private void switchToControls() {
        binding.callControlLayout.setVisibility(View.VISIBLE);
    }

    private void callJavaScriptFunction(String functionName) {

        // Since calling the JS functions from server.js can't be run on Main Thread... we take the help of child threads
        binding.webView.post(new Runnable() {
            @Override
            public void run() {
                binding.webView.evaluateJavascript(functionName, null);
            }
        });
    }

    // Helper method to convert InputStream to File
    private File convertInputStreamToFile(InputStream inputStream) throws IOException {
        File file = new File(getCacheDir(), "temp.html");
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
        return file;
    }


    private static class CustomWebChromeClient extends WebChromeClient {
        @Override
        public void onPermissionRequest(final PermissionRequest request) {
            // Check the requested permissions
            String[] requestedPermissions = request.getResources();
            for (String permission : requestedPermissions) {
                // Handle the requested permissions as needed
                if (permission.equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                    // Grant permission for video capture
                    request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
                } else if (permission.equals(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
                    // Grant permission for audio capture
                    request.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});
                } else {
                    // Deny permission for other resources
                    request.deny();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        binding.webView.loadUrl("about:blank");
        super.onDestroy();
    }
}






