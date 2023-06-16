package com.example.chatapp.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.Models.HistoryCall;
import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.example.chatapp.Utilities.Utilities;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.URL;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class VideoCallComingActivity extends AppCompatActivity {
    CircleImageView cirAvatarVideoCalComing;
    TextView tvNameVideoCalComing, tvEmailVideoCallComing;
    FloatingActionButton fabDeclineCall, fabAcceptVideoCall;
    DatabaseReference mUserReference, mVideoCallReference, mHistoryCallReference;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    String senderName, senderID, senderAvatar, receiveID, type="VideoCall";
    FirebaseUser mUser;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call_coming);
        setControl();
        setEvent();
    }

    private void setControl() {
        cirAvatarVideoCalComing = findViewById(R.id.cirAvatarVideoCalComing);
        tvNameVideoCalComing = findViewById(R.id.tvNameVideoCalComing);
        tvEmailVideoCallComing = findViewById(R.id.tvEmailVideoCallComing);
        fabDeclineCall = findViewById(R.id.fabDeclineCall);
        fabAcceptVideoCall = findViewById(R.id.fabAcceptVideoCall);

        mUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mVideoCallReference = FirebaseDatabase.getInstance().getReference().child("VideoCallComing");
        mHistoryCallReference = FirebaseDatabase.getInstance().getReference().child("HistoryCall");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        senderID = getIntent().getStringExtra("senderID");
        receiveID = mUser.getUid();
    }

    private void setEvent(){
        loadProfileSender();
        checkResponse();

        fabAcceptVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String response = "yes", callTime =  Utilities.getCurrentTime("dd/MM/yyyy, hh:mm a");
                sendResponse(response);
                String historyCallId = Utilities.getHistoryCallId();
                long timestamp = System.currentTimeMillis();
                HistoryCall historyCall = new HistoryCall(historyCallId,senderAvatar,senderID,senderName,"ReceiveCall",type,callTime, timestamp);
                historyCall.updateHistoryCall(mHistoryCallReference,historyCall,receiveID, historyCallId);
            }
        });

        fabDeclineCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               String response = "no", callTime =  Utilities.getCurrentTime("dd/MM/yyyy, hh:mm a");
               sendResponse(response);
                long timestamp = System.currentTimeMillis();
                String historyCallId = Utilities.getHistoryCallId();
                HistoryCall historyCall = new HistoryCall(historyCallId,senderAvatar,senderID,senderName,"ReceiveCall",type,callTime, timestamp);
                historyCall.updateHistoryCall(mHistoryCallReference,historyCall,receiveID, historyCallId);
            }
        });
    }

    private void sendResponse(String response) {
        if (response.equals("yes")){
            HashMap hashMap = new HashMap();
            hashMap.put("key",senderName+receiveID);
            hashMap.put("response","yes");
            mVideoCallReference.child(senderID).child(receiveID).child("response").updateChildren(hashMap);
            joinMeeting();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mVideoCallReference.child(senderID).child(receiveID).removeValue();
                }
            },3000);
        } else if (response.equals("no")){
            HashMap hashMap = new HashMap();
            hashMap.put("key",senderName+receiveID);
            hashMap.put("response","no");
            mVideoCallReference.child(senderID).child(receiveID).child("response").updateChildren(hashMap);
            finish();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mVideoCallReference.child(senderID).child(receiveID).removeValue();
                }
            },1000);
        }

    }


    private void loadProfileSender() {
        mUserReference.child(senderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    senderName = user.getUserName();
                    senderAvatar = user.getProfilePic();
                    Picasso.get().load(senderAvatar).placeholder(R.drawable.default_avatar).into(cirAvatarVideoCalComing);
                    tvNameVideoCalComing.setText(user.getUserName());
                    tvEmailVideoCallComing.setText(user.getEmail());
                } else {
                    Toast.makeText(VideoCallComingActivity.this, "Không tìm thấy dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void joinMeeting() {
        try {
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(new URL("https://meet.jit.si"))
                    .setRoom(senderName+receiveID)
                    .setFeatureFlag("welcomepage.enabled", false)
                    .setFeatureFlag("prejoinpage.enabled",false)
                    .build();
            JitsiMeetActivity.launch(VideoCallComingActivity.this, options);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkResponse() {
        mVideoCallReference.child(senderID).child(receiveID).child("response").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String response = snapshot.child("response").getValue().toString().trim();
                    if (response.equals("no")) {
                        finish();
                    } else {
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    /* Xét trạng thái hoạt động của CurrentUser */
    @Override
    protected void onStart(){
        Utilities.statusActivity("Online");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Utilities.statusActivity("Online");
        super.onResume();
    }
    @Override
    protected void onRestart() {
        Utilities.statusActivity("Online");
        super.onRestart();
    }
}