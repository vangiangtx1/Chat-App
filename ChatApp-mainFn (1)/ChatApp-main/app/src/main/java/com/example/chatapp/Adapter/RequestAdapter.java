package com.example.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.Models.Friend;
import com.example.chatapp.Models.Request;
import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.example.chatapp.View.ViewItemContactActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
    String profilePicURL, userName, email, describe, gender, userId;
    Context context;
    ArrayList<Request> requestsList;

    public RequestAdapter(Context context, ArrayList<Request> requestsList) {
        this.context = context;
        this.requestsList = requestsList;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new RequestViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mRequestReference = FirebaseDatabase.getInstance().getReference().child("Requests");
        DatabaseReference mFriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        Request requests = requestsList.get(position);
        if (requests == null) {
            return;
        } else {
            holder.tvItemRequest.setText(requests.getUserName());
            Picasso.get().load(requests.getProfilePic()).placeholder(R.drawable.default_avatar).into(holder.civAvatarItemRequest);

            // Ấn nút đồng ý kết bạn
            holder.btnConfirmRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mRequestReference.child(myId).child(requests.getUserID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mRequestReference.child(requests.getUserID()).removeValue();
                        }
                    });
                    mFriendsReference.child(myId).child(requests.getUserID()).setValue(requests.getUserID()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mFriendsReference.child(requests.getUserID()).child(myId).setValue(myId).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if ((task.isSuccessful())) {
                                            Toast.makeText(view.getContext(), "Các bạn đã trở thành bạn bè", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            });

            //Ấn nút hủy
            holder.btnRefuseRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mRequestReference.child(myId).child(requests.getUserID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mRequestReference.child(requests.getUserID()).removeValue();
                        }
                    });
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String userID = requestsList.get(holder.getAdapterPosition()).getUserID();
                    Intent intent = new Intent(holder.itemView.getContext(), ViewItemContactActivity.class);
                    intent.putExtra("userID", userID);
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return requestsList.size();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        CircleImageView civAvatarItemRequest;
        TextView tvItemRequest;
        Button btnConfirmRequest, btnRefuseRequest;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            civAvatarItemRequest = itemView.findViewById(R.id.civAvatarItemRequest);
            tvItemRequest = itemView.findViewById(R.id.tvItemRequest);
            btnConfirmRequest = itemView.findViewById(R.id.btnConfirmRequest);
            btnRefuseRequest = itemView.findViewById(R.id.btnRefuseRequest);
        }
    }
}
