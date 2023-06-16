package com.example.chatapp.View;

import static com.example.chatapp.MainActivity.MY_REQUEST_CODE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.Adapter.MessageAdapter;
import com.example.chatapp.MainActivity;
import com.example.chatapp.Models.Chat;
import com.example.chatapp.Models.HistoryCall;
import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.example.chatapp.Utilities.Utilities;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    FirebaseRecyclerOptions<Chat> optionsMessage;
    FirebaseRecyclerAdapter<Chat, MessageAdapter> messageAdapter;

    Toolbar chat_toolbar;
    LinearLayout userChat;
    RecyclerView rvMessage;
    EditText edtInputMessage;
    ImageView imageViewSendImage, imageViewSendMessage, imageViewSendEnmoji, ivStatusActivity;
    CircleImageView civAvatarUserChat;
    TextView tvUserNameToolChat, tvUserOnl_OffChat;
    String userID, avatarURL, userName, dateTime, statusActivity, time;
    String myAvatar, myId, myName, myUserEmail;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mUserReference, mFriendsReference, mSmsReference, mChatReference, mHistoryCallReference;
    StorageReference mStorageReference;
    FirebaseStorage storage;
    Uri image_uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setControl();
        setEvent();

    }

    private void setControl() {
        chat_toolbar = findViewById(R.id.chat_toolbar);
        userChat = findViewById(R.id.userChat);
        edtInputMessage = findViewById(R.id.edtInputMessage);
        imageViewSendImage = findViewById(R.id.imageViewSendImage);
        imageViewSendEnmoji = findViewById(R.id.imageViewSendEnmoji);
        imageViewSendMessage = findViewById(R.id.imageViewSendMessage);
        rvMessage = findViewById(R.id.rvMessage);
        rvMessage.setLayoutManager(new LinearLayoutManager(this));
        tvUserNameToolChat = findViewById(R.id.tvUserNameToolChat);
        tvUserOnl_OffChat = findViewById(R.id.tvUserOnl_OffChat);
        civAvatarUserChat = findViewById(R.id.civAvatarUserChat);
        ivStatusActivity = findViewById(R.id.ivStatusActivity);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mSmsReference = FirebaseDatabase.getInstance().getReference().child("Messages");
        mFriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        mChatReference = FirebaseDatabase.getInstance().getReference().child("Chats");
        mHistoryCallReference = FirebaseDatabase.getInstance().getReference().child("HistoryCall");
        storage = FirebaseStorage.getInstance();
        mStorageReference = storage.getReference().child("profilePic/default_avatar.png");
        userID = getIntent().getStringExtra("userID");
        myId = mUser.getUid();
        setSupportActionBar(chat_toolbar);
    }

    private void setEvent() {
        actionToolBar();
        loadInformationUserChat(userID);
        loadMyProfile();
        loadSMS();


        userChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnClickAvatar(userID);
            }
        });

        imageViewSendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        imageViewSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendSMS();
            }
        });

    }

    private void actionToolBar() {
        chat_toolbar.setTitle("");
        setSupportActionBar(chat_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        chat_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });
    }

    private void loadInformationUserChat(String userID) {
        mUserReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    avatarURL = user.getProfilePic();
                    userName = user.getUserName();
                    statusActivity = user.getStatusActivity();

                    Picasso.get().load(avatarURL).placeholder(R.drawable.default_avatar).into(civAvatarUserChat);
                    tvUserNameToolChat.setText(userName);
                    tvUserOnl_OffChat.setText(statusActivity);

                    if (statusActivity.trim().equals("Online")) {
                        ivStatusActivity.setImageResource(R.drawable.icon_online);
                    } else {
                        ivStatusActivity.setImageResource(R.drawable.icon_offline);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMyProfile() {
        mUserReference.child(myId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    myName = user.getUserName();
                    myUserEmail = user.getEmail();
                    myAvatar = user.getProfilePic();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadSMS() {
        optionsMessage = new FirebaseRecyclerOptions.Builder<Chat>().setQuery(mSmsReference.child(myId).child(userID), Chat.class).build();
        messageAdapter = new FirebaseRecyclerAdapter<Chat, MessageAdapter>(optionsMessage) {

            @Override
            protected void onBindViewHolder(@NonNull MessageAdapter holder, int position, @NonNull Chat model) {
                if (model.getSenderID().equals(myId) && model.getType().equals("text")) {
                    holder.civAvatarUserOne.setVisibility(View.GONE);
                    holder.tvSmsUserOne.setVisibility(View.GONE);
                    holder.tvTimeMessageUserOne.setVisibility(View.GONE);
                    holder.tvSmsUserTwo.setVisibility(View.VISIBLE);
                    holder.tvSmsUserTwo.setText(model.getMessage());
                    holder.tvTimeMessageUserTwo.setVisibility(View.VISIBLE);
                    holder.tvTimeMessageUserTwo.setText(model.getDateTime());
                    holder.ivImageLeft.setVisibility(View.GONE);
                    holder.ivImageRight.setVisibility(View.GONE);
                    holder.tvTimeImageUserOne.setVisibility(View.GONE);
                    holder.tvTimeImageUserTwo.setVisibility(View.GONE);
                } else if (model.getSenderID().equals(myId) && model.getType().equals("image")) {
                    holder.civAvatarUserOne.setVisibility(View.GONE);
                    holder.tvSmsUserOne.setVisibility(View.GONE);
                    holder.tvTimeMessageUserOne.setVisibility(View.GONE);
                    holder.tvSmsUserTwo.setVisibility(View.GONE);
                    holder.tvTimeMessageUserTwo.setVisibility(View.GONE);
                    holder.ivImageLeft.setVisibility(View.GONE);
                    holder.tvTimeImageUserOne.setVisibility(View.GONE);
                    holder.ivImageRight.setVisibility(View.VISIBLE);
                    Picasso.get().load(model.getMessage()).placeholder(R.drawable.ic_image_black).into(holder.ivImageRight);
                    holder.tvTimeImageUserTwo.setVisibility(View.VISIBLE);
                    holder.tvTimeImageUserTwo.setText(model.getDateTime());
                } else if (model.getSenderID().equals(userID) && model.getType().equals("text")) {
                    holder.civAvatarUserOne.setVisibility(View.VISIBLE);
                    Picasso.get().load(model.getProfilePic()).placeholder(R.drawable.default_avatar).into(holder.civAvatarUserOne);
                    holder.tvSmsUserOne.setVisibility(View.VISIBLE);
                    holder.tvSmsUserOne.setText(model.getMessage());
                    holder.tvTimeMessageUserOne.setVisibility(View.VISIBLE);
                    holder.tvTimeMessageUserOne.setText(model.getDateTime());
                    holder.tvSmsUserTwo.setVisibility(View.GONE);
                    holder.tvTimeMessageUserTwo.setVisibility(View.GONE);
                    holder.ivImageLeft.setVisibility(View.GONE);
                    holder.ivImageRight.setVisibility(View.GONE);
                    holder.tvTimeImageUserOne.setVisibility(View.GONE);
                    holder.tvTimeImageUserTwo.setVisibility(View.GONE);
                } else if (model.getSenderID().equals(userID) && model.getType().equals("image")) {
                    holder.civAvatarUserOne.setVisibility(View.VISIBLE);
                    Picasso.get().load(model.getProfilePic()).placeholder(R.drawable.default_avatar).into(holder.civAvatarUserOne);
                    holder.tvSmsUserOne.setVisibility(View.GONE);
                    holder.tvTimeMessageUserOne.setVisibility(View.GONE);
                    holder.tvSmsUserTwo.setVisibility(View.GONE);
                    holder.tvTimeMessageUserTwo.setVisibility(View.GONE);
                    holder.ivImageLeft.setVisibility(View.VISIBLE);
                    Picasso.get().load(model.getMessage()).placeholder(R.drawable.ic_image_black).into(holder.ivImageLeft);
                    holder.ivImageRight.setVisibility(View.GONE);
                    holder.tvTimeImageUserOne.setVisibility(View.VISIBLE);
                    holder.tvTimeImageUserOne.setText(model.getDateTime());
                    holder.tvTimeImageUserTwo.setVisibility(View.GONE);
                }
            }

            @NonNull
            @Override
            public MessageAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_single_sms, parent, false);
                return new MessageAdapter(view);
            }
        };
        messageAdapter.startListening();
        rvMessage.setAdapter(messageAdapter);
    }

    private void btnClickAvatar(String userID) {
        mFriendsReference.child(myId).child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Intent intent = new Intent(ChatActivity.this, ViewSingleFriendActivity.class);
                    intent.putExtra("userID", userID).toString();
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(ChatActivity.this, ViewItemContactActivity.class);
                    intent.putExtra("userID", userID).toString();
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, MY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            if (data.getData() != null) {
                image_uri = data.getData();
                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

    private void sendImageMessage(Uri image_uri) throws IOException {
        long timestamp = System.currentTimeMillis();
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang gửi");
        progressDialog.show();
        String fileName = "post_" + timestamp;

        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);
        ByteArrayOutputStream bitmapOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bitmapOutputStream);
        byte[] data = bitmapOutputStream.toByteArray();
        StorageReference myStorageReference = FirebaseStorage.getInstance().getReference().child("ChatImages").child(myId).child(userID).child(fileName);
        StorageReference userStorageReference = FirebaseStorage.getInstance().getReference().child("ChatImages").child(userID).child(myId).child(fileName);
        myStorageReference.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        userStorageReference.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //imageUpload
                                progressDialog.dismiss();
                                //getUrl of upload image
                                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                while (!uriTask.isSuccessful()) ;
                                String downloadUri = uriTask.getResult().toString();

                                if (uriTask.isSuccessful()) {
                                    String dateTime = Utilities.getCurrentTime("dd/MM/yyyy, hh:mm a");
                                    long timestamp = System.currentTimeMillis();
                                    String type = "image";
                                    Chat message01 = new Chat(userName, avatarURL, downloadUri, myId, userID, dateTime, timestamp, type);
                                    Chat message02 = new Chat(myName, myAvatar, downloadUri, myId, userID, dateTime, timestamp, type);
                                    Utilities.updateDataWithPush(mSmsReference, myId, userID, message01, message02);
                                    createChatBox(downloadUri, timestamp, type);
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Failure
                        progressDialog.dismiss();
                    }
                });
    }

    private void SendSMS() {
        String sms = edtInputMessage.getText().toString().trim();

        if (sms.isEmpty()) {
            Toast.makeText(this, "Tin nhắn không được để trống", Toast.LENGTH_SHORT).show();
        } else {
            String dateTime = Utilities.getCurrentTime("dd/MM/yyyy, hh:mm a");
            long timestamp = System.currentTimeMillis();
            String type = "text";
            Chat message01 = new Chat(userName, avatarURL, sms, myId, userID, dateTime, timestamp, type);
            Chat message02 = new Chat(myName, myAvatar, sms, myId, userID, dateTime, timestamp, type);
            Utilities.updateDataWithPush(mSmsReference, myId, userID, message01, message02);
            edtInputMessage.setText(null);
            createChatBox(sms, timestamp, type);
        }
    }

    private void createChatBox(String sms, long timestamp, String type) {
        String dateTime = Utilities.getCurrentTime("HH:mm");
        Chat chat = new Chat(userName, avatarURL, sms, userID, myId, userID, dateTime, type, timestamp);
        Chat chat1 = new Chat(myName, myAvatar, sms, myId, myId, userID, dateTime, type, timestamp);
        Utilities.updateDataWithoutNotify(mChatReference, myId, userID, chat);
        Utilities.updateDataWithoutNotify(mChatReference, userID, myId, chat1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_view_profile:
                mFriendsReference.child(myId).child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Intent intent = new Intent(ChatActivity.this, ViewSingleFriendActivity.class);
                            intent.putExtra("userID", userID).toString();
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(ChatActivity.this, ViewItemContactActivity.class);
                            intent.putExtra("userID", userID).toString();
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                break;
            case R.id.action_deleteChatbox:
                openDialogConfirmDeleteChatbox(Gravity.CENTER);
                break;
            case R.id.action_voiceCall:
                requestPermissionForVoiceCall();
                break;
            case R.id.action_videoCall:
                requestPermissionForVideoCall();
                break;
            default:
                break;
        }
        return true;
    }

    private void openDialogConfirmDeleteChatbox(int gravity) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_delete_chatbox);
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
            Button btnConfirmDeleteChat = dialog.findViewById(R.id.btnConfirmDeleteChat);
            Button btnCancelConfirmDeleteChat = dialog.findViewById(R.id.btnCancelConfirmDeleteChat);

            btnConfirmDeleteChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ProgressDialog dialogMessage = new ProgressDialog(ChatActivity.this);
                    dialogMessage.setTitle("Xóa đoạn tin nhắn");
                    dialogMessage.setMessage("Vui lòng đợi...");
                    dialogMessage.show();
                    mSmsReference.child(myId).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mChatReference.child(myId).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(ChatActivity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                                            dialogMessage.dismiss();
                                            dialog.dismiss();
                                            Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }

                                    }
                                });
                            }
                        }
                    });
                }
            });

            btnCancelConfirmDeleteChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
        }
        dialog.show();
    }

    private void requestPermissionForVoiceCall() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                String type = "VoiceCall", status = "MakeCall", callTime = Utilities.getCurrentTime("dd/MM/yyyy, hh:mm a");
                String historyCallId = Utilities.getHistoryCallId();
                long timestamp = System.currentTimeMillis();
                HistoryCall historyCall = new HistoryCall(historyCallId, avatarURL, userID, userName, status, type, callTime, timestamp);
                historyCall.updateHistoryCall(mHistoryCallReference, historyCall, myId, historyCallId);
                Intent intent = new Intent(ChatActivity.this, VoiceCallOutGoingActivity.class);
                intent.putExtra("receiverID", userID);
                startActivity(intent);
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(ChatActivity.this, "Quyền bị từ chối\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };
        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("Nếu bạn từ chối quyền, bạn không thể sử dụng dịch vụ này\n\nVui lòng bật quyền tại [Setting] > [Permission]")
                .setPermissions(Manifest.permission.RECORD_AUDIO)
                .check();
    }

    private void requestPermissionForVideoCall() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                String type = "VideoCall", status = "MakeCall", callTime = Utilities.getCurrentTime("dd/MM/yyyy, hh:mm a");
                String historyCallId = Utilities.getHistoryCallId();
                long timestamp = System.currentTimeMillis();
                HistoryCall historyCall = new HistoryCall(historyCallId, avatarURL, userID, userName, status, type, callTime, timestamp);
                historyCall.updateHistoryCall(mHistoryCallReference, historyCall, myId, historyCallId);
                Intent intent = new Intent(ChatActivity.this, VideoCallOutgoingActivity.class);
                intent.putExtra("friendID", userID);
                startActivity(intent);
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(ChatActivity.this, "Quyền bị từ chối\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };
        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("Nếu bạn từ chối quyền, bạn không thể sử dụng dịch vụ này\n\nVui lòng bật quyền tại [Setting] > [Permission]")
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                .check();
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