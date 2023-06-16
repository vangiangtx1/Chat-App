package com.example.chatapp.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.Models.Friend;
import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.example.chatapp.Utilities.Utilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewSingleFriendActivity extends AppCompatActivity {
    Toolbar toolbar_singleFriend;
    CircleImageView civAvatarSingleFriend;
    ImageView civSingleFriendStatusActivity;
    TextView tvDescribeSingleFriend, tvUserNameSingleFriend, tvEmailSingleFriend, tvGenderSingleFriend;
    Button btnSendMessage, btnUnfriend;

    String friendID, profilePicURL, userName, email, gender, describe, statusActivity, currentStatus = "friend";

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mFriendReference, mUserReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_single_friend);
        setControl();
        callInformationFriend(friendID);
        SetEvent();

    }


    private void setControl() {
        toolbar_singleFriend = findViewById(R.id.toolbar_singleFriend);
        civAvatarSingleFriend = findViewById(R.id.civAvatarSingleFriend);
        civSingleFriendStatusActivity = findViewById(R.id.civSingleFriendStatusActivity);
        tvDescribeSingleFriend = findViewById(R.id.tvDescribeSingleFriend);
        tvUserNameSingleFriend = findViewById(R.id.tvUserNameSingleFriend);
        tvEmailSingleFriend = findViewById(R.id.tvEmailSingleFriend);
        tvGenderSingleFriend = findViewById(R.id.tvGenderSingleFriend);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        btnUnfriend = findViewById(R.id.btnUnfriend);
        friendID = getIntent().getStringExtra("userID");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mFriendReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        mUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    private void SetEvent() {
        actionToolBar();

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnSendMessage(friendID);
            }
        });

        btnUnfriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openConfirmUnfriendDialog(Gravity.CENTER);
            }
        });
    }

    private void actionToolBar() {
        setSupportActionBar(toolbar_singleFriend);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Hồ sơ bạn bè");
        toolbar_singleFriend.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });
    }

    private void btnSendMessage(String friendID) {
        Intent intent = new Intent(ViewSingleFriendActivity.this, ChatActivity.class);
        intent.putExtra("userID", friendID).toString();
        startActivity(intent);
    }

    private void unFriend(String friendID) {

        mFriendReference.child(mUser.getUid()).child(friendID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mFriendReference.child(friendID).child(mUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ViewSingleFriendActivity.this, "Hủy kết bạn thành công", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ViewSingleFriendActivity.this, ViewItemContactActivity.class);
                                intent.putExtra("userID", friendID);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                }
            }
        });
    }

    private void openConfirmUnfriendDialog(int gravity) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_unfriend);
        Window window = (Window) dialog.getWindow();
        if (window == null) {
            return;
        } else {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            WindowManager.LayoutParams windowAttributes = window.getAttributes();
            window.setAttributes(windowAttributes);

            if (Gravity.CENTER == gravity) {
                dialog.setCancelable(true);
            } else {
                dialog.setCancelable(false);
            }
            Button btnConfirm = dialog.findViewById(R.id.btnConfirmUnfriend);
            Button btnCancelConfirm = dialog.findViewById(R.id.btnCancelConfirmUnfriend);

            btnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    unFriend(friendID);
                    dialog.dismiss();

                }
            });
            btnCancelConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
        }
        dialog.show();
    }

private void callInformationFriend(String friendID) {

    mUserReference.child(friendID).addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists()) {
                User friend = snapshot.getValue(User.class);
                Picasso.get().load(friend.getProfilePic()).placeholder(R.drawable.default_avatar).into(civAvatarSingleFriend);
                tvDescribeSingleFriend.setText(friend.getDescribe());
                tvUserNameSingleFriend.setText(friend.getUserName());
                tvEmailSingleFriend.setText(friend.getEmail());
                tvGenderSingleFriend.setText(friend.getGender());
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });

    mUserReference.child(friendID).addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists()) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    if (user.getStatusActivity().equals("Online")) {
                        civSingleFriendStatusActivity.setImageResource(R.drawable.icon_online);
                    } else {
                        civSingleFriendStatusActivity.setImageResource(R.drawable.icon_offline);
                    }
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });
}
    /* Xét trạng thái hoạt động của CurrentUser */
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

    @Override
    protected void onDestroy() {
        Utilities.statusActivity("Offline");
        super.onDestroy();
    }
}