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
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.Models.Friend;
import com.example.chatapp.Models.Request;
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

public class ViewItemContactActivity extends AppCompatActivity {
    Toolbar toolbar_singleContact;
    TextView tvDescribeSingleContact, tvUserNameSingleContact, tvEmailSingleContact;
    Button btnSendFriendRequest, btnCancelSendFriendRequest;
    CircleImageView civAvatarSingleContact;
    String userAvatar, userName, status, userEmail, userGender, userDescribe, userID, currentState;
    String myAvatar, myUsername, myEmail, myGender, myDescribe, myUserID;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mUserReference, mRequestReference, mFriendsReference;
    FirebaseStorage storage;
    StorageReference mStorageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_item_contact);
        setControl();
        setEvent();
    }


    private void setControl() {
        toolbar_singleContact = findViewById(R.id.toolbar_singleContact);
        civAvatarSingleContact = findViewById(R.id.civAvatarSingleContact);
        tvDescribeSingleContact = findViewById(R.id.tvDescribeSingleContact);
        tvUserNameSingleContact = findViewById(R.id.tvUserNameSingleContact);
        tvEmailSingleContact = findViewById(R.id.tvEmailSingleContact);
        btnSendFriendRequest = findViewById(R.id.btnSendFriendRequest);
        btnCancelSendFriendRequest = findViewById(R.id.btnCancelSendFriendRequest);
        userID = getIntent().getStringExtra("userID"); // Sử dụng put/getExtra để truyền dữ liệu từ ContactAdapter sang ViewItemContactActivity
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mRequestReference = FirebaseDatabase.getInstance().getReference().child("Requests");
        mFriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        storage = FirebaseStorage.getInstance();
        mStorageReference = storage.getReference().child("profilePic/default_avatar.png");
        currentState = "nothing_happen";
    }


    private void setEvent() {
        setActionToolbar();
        /* Xử lý sự kiện nút GỬI kết bạn */
        btnSendFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendAction(userID);
            }
        });
        /* Xử lý sự kiện nút HỦY kết bạn */
        btnCancelSendFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelAction(userID);
            }
        });
        checkUserExistance(userID); //Check trạng thái của Request

        callInformationItemContact(); // Hiển thị thông tin của người dùng khi click vào
        loadMyProfile(); //Load dữ liệu của bản thân
        loadOtherContactProfile();
    }

    private void loadOtherContactProfile() {
        mUserReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        userAvatar = user.getProfilePic();
                        userName = user.getUserName();
                        userEmail = user.getEmail();
                        userDescribe = user.getDescribe();
                        userGender = user.getGender();
                    }
                } else {
                    Toast.makeText(ViewItemContactActivity.this, "Không tìm thấy dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewItemContactActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setActionToolbar() {
        setSupportActionBar(toolbar_singleContact);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Hồ sơ người dùng");
        toolbar_singleContact.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });
    }

    private void loadMyProfile() {
        mUserReference.child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        myAvatar = user.getProfilePic();
                        myUserID = user.getUserID();
                        myUsername = user.getUserName();
                        myEmail = user.getEmail();
                        myDescribe = user.getDescribe();
                        myGender = user.getGender();

                    }
                } else {
                    Toast.makeText(ViewItemContactActivity.this, "Không tìm thấy dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewItemContactActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void callInformationItemContact() {
        mUserReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        Picasso.get().load(user.getProfilePic()).placeholder(R.drawable.default_avatar).into(civAvatarSingleContact);
                        tvDescribeSingleContact.setText(user.getDescribe());
                        tvUserNameSingleContact.setText(user.getUserName());
                        tvEmailSingleContact.setText(user.getEmail());
                    }
                } else {
                    Toast.makeText(ViewItemContactActivity.this, "Không tìm thấy dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewItemContactActivity.this, "" + error.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void sendAction(String userID) {

        if (currentState.equals("nothing_happen")) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("status", "pending");
            mRequestReference.child(mUser.getUid()).child(userID).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Request request = new Request(myUsername, myAvatar, myUserID, "wait_confirm");
                        mRequestReference.child(userID).child(mUser.getUid()).setValue(request).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ViewItemContactActivity.this, "Bạn đã gửi lời mời kết bạn", Toast.LENGTH_SHORT).show();
                                    btnCancelSendFriendRequest.setVisibility(View.GONE);
                                    currentState = "i_sent_pending";
                                    btnSendFriendRequest.setText(R.string.cancel_request);
                                } else {
                                    Toast.makeText(ViewItemContactActivity.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        }
        if (currentState.equals("i_sent_pending") || currentState.equals("i_sent_decline")) {
            mRequestReference.child(mUser.getUid()).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        mRequestReference.child(userID).child(mUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ViewItemContactActivity.this, "Bạn đã hủy yêu cầu kết bạn", Toast.LENGTH_SHORT).show();
                                    currentState = "nothing_happen";
                                    btnSendFriendRequest.setText(R.string.add_friend);
                                    btnCancelSendFriendRequest.setVisibility(View.GONE);
                                } else {
                                    Toast.makeText(ViewItemContactActivity.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        }
        if (currentState.equals("he_sent_pending")) {
            mRequestReference.child(userID).child(mUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        mRequestReference.child(mUser.getUid()).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
//                                    Friend friend = new Friend(userAvatar, userName, userEmail, userDescribe, userGender, userID);
//                                    Friend me = new Friend(myAvatar, myUsername, myEmail, myDescribe, myGender, myUserID);

                                    mFriendsReference.child(mUser.getUid()).child(userID).setValue(userID).addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
                                            if (task.isSuccessful()) {
                                                mFriendsReference.child(userID).child(mUser.getUid()).setValue(myUserID).addOnCompleteListener(new OnCompleteListener() {
                                                    @Override
                                                    public void onComplete(@NonNull Task task) {
                                                        currentState = "friend";
                                                        btnSendFriendRequest.setText(R.string.send_message);
                                                        btnCancelSendFriendRequest.setText(R.string.unfriend);
                                                        btnCancelSendFriendRequest.setVisibility(View.VISIBLE);
                                                        Toast.makeText(ViewItemContactActivity.this, "Các bạn đã là bạn bè", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(ViewItemContactActivity.this, ViewSingleFriendActivity.class);
                                                        intent.putExtra("userID", userID);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }

                }
            });
        }
        if (currentState.equals("friend")) {
            Intent intent = new Intent(ViewItemContactActivity.this, ChatActivity.class);
            intent.putExtra("userID", userID);
            startActivity(intent);
        }
    }

    private void checkUserExistance(String userID) {
        mFriendsReference.child(mUser.getUid()).child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentState = "friend";
                    btnSendFriendRequest.setText(R.string.send_message);
                    btnCancelSendFriendRequest.setText(R.string.unfriend);
                    btnCancelSendFriendRequest.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mFriendsReference.child(userID).child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentState = "friend";
                    btnSendFriendRequest.setText(R.string.send_message);
                    btnCancelSendFriendRequest.setText(R.string.unfriend);
                    btnCancelSendFriendRequest.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mRequestReference.child(mUser.getUid()).child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("status").getValue().toString().equals("pending")) {
                        currentState = "i_sent_pending";
                        btnSendFriendRequest.setText(R.string.cancel_request);
                        btnCancelSendFriendRequest.setVisibility(View.GONE);
                    }
                    if (snapshot.child("status").getValue().toString().equals("decline")) {
                        currentState = "i_sent_decline";
                        btnSendFriendRequest.setText(R.string.cancel_request);
                        btnCancelSendFriendRequest.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mRequestReference.child(mUser.getUid()).child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("status").getValue().toString().equals("wait_confirm")) {
                        mRequestReference.child(userID).child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    if (snapshot.child("status").getValue().toString().equals("pending")) {
                                        currentState = "he_sent_pending";
                                        btnSendFriendRequest.setText(R.string.accept);
                                        btnCancelSendFriendRequest.setText(R.string.decline);
                                        btnCancelSendFriendRequest.setVisibility(View.VISIBLE);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (currentState.equals("nothing_happen")) {
            currentState = "nothing_happen";
            btnSendFriendRequest.setText(R.string.add_friend);
            btnCancelSendFriendRequest.setVisibility(View.GONE);
        }
    }

    private void cancelAction(String userID) {
        if (currentState.equals("friend")) {
            openConfirmUnfriendDialog(Gravity.CENTER);
        }
        if (currentState.equals("he_sent_pending")) {
            mRequestReference.child(userID).child(mUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener() {

                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        mRequestReference.child(mUser.getUid()).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ViewItemContactActivity.this, "Đã từ chối kết bạn", Toast.LENGTH_SHORT).show();
                                    currentState = "nothing_happen";
                                    btnSendFriendRequest.setText(R.string.add_friend);
                                    btnCancelSendFriendRequest.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                }
            });
        }
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
                    dialog.dismiss();
                    deleteFriend(userID);
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

    private void deleteFriend(String userID) {
        mFriendsReference.child(mUser.getUid()).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mFriendsReference.child(userID).child(mUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ViewItemContactActivity.this, "Đã hủy kết bạn", Toast.LENGTH_SHORT).show();
                                currentState = "nothing_happen";
                                btnSendFriendRequest.setText(R.string.add_friend);
                                btnCancelSendFriendRequest.setVisibility(View.GONE);
                            }

                        }
                    });
                }
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