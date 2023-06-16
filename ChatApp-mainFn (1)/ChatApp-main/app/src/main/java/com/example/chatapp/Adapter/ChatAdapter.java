package com.example.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.Models.Chat;
import com.example.chatapp.R;
import com.example.chatapp.View.ChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> implements Filterable {

    Context context;
    ArrayList<Chat> listChats;
    ArrayList<Chat> listFilterChatts;
    FirebaseUser mUser;
    DatabaseReference mDatabaseReference, mChatReference;

    public ChatAdapter(Context context, ArrayList<Chat> listChats) {
        this.context = context;
        this.listChats = listChats;
        this.listFilterChatts = listChats;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mChatReference = FirebaseDatabase.getInstance().getReference().child("Chats");
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        Chat chat = listChats.get(position);
        if (chat == null) {
            return;
        } else {
            Picasso.get().load(chat.getProfilePic()).placeholder(R.drawable.default_avatar).into(holder.civAvatarItemChat);
            holder.tvItemChatName.setText(chat.getUserName());
            holder.tvTimeLastMessage.setText(chat.getDateTime());
            mChatReference.child(mUser.getUid()).child(chat.getFriendID()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Chat chat1 = snapshot.getValue(Chat.class);
                        String empty = " ";
                        if (chat1 != null) {
                            if (chat1.getType().equals("text") && chat1.getSenderID().equals(mUser.getUid())) {
                                holder.tvPersonSend.setVisibility(View.VISIBLE);
                                holder.tvPersonSend.setText("Bạn: ");
                                holder.tvLastMessage.setText(chat1.getMessage());
                            } else if (chat1.getType().equals("image") && chat1.getSenderID().equals(mUser.getUid())) {
                                holder.tvPersonSend.setVisibility(View.VISIBLE);
                                holder.tvPersonSend.setText("Bạn");
                                holder.tvLastMessage.setText(R.string.send_image);
                            } else if (chat1.getType().equals("text") && chat1.getSenderID().equals(chat.getFriendID())) {
                                holder.tvPersonSend.setVisibility(View.GONE);
                                holder.tvLastMessage.setText(chat1.getMessage());
                            } else if (chat1.getType().equals("image") && chat1.getSenderID().equals(chat.getFriendID())) {
                                holder.tvPersonSend.setVisibility(View.VISIBLE);
                                holder.tvPersonSend.setText(chat.getUserName());
                                holder.tvLastMessage.setText(R.string.send_image);
                            }
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            mDatabaseReference.child(chat.getFriendID()).child("statusActivity").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        if (snapshot.getValue().toString().equals("Online")) {
                            holder.ivItemChatStatusActivity.setImageResource(R.drawable.icon_online);
                        } else {
                            holder.ivItemChatStatusActivity.setImageResource(R.drawable.icon_offline);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String userID = listChats.get(holder.getAdapterPosition()).getFriendID();
                    Intent intent = new Intent(holder.itemView.getContext(), ChatActivity.class);
                    intent.putExtra("userID", userID);
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listChats.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView civAvatarItemChat;
        public ImageView ivItemChatStatusActivity;
        public TextView tvItemChatName, tvLastMessage, tvTimeLastMessage, tvPersonSend;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            civAvatarItemChat = itemView.findViewById(R.id.civAvatarItemChat);
            ivItemChatStatusActivity = itemView.findViewById(R.id.ivItemChatStatusActivity);
            tvItemChatName = itemView.findViewById(R.id.tvItemChatName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTimeLastMessage = itemView.findViewById(R.id.tvTimeLastMessage);
            tvPersonSend = itemView.findViewById(R.id.tvPersonSend);
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String strSearch = charSequence.toString();
                if (strSearch.isEmpty()) {
                    listChats = listFilterChatts;
                } else {
                    ArrayList<Chat> list = new ArrayList<>();
                    for (Chat chat : listFilterChatts) {
                        if (chat.getUserName().toString().toLowerCase().trim().contains(strSearch.toLowerCase().trim())) {
                            list.add(chat);
                        }
                    }
                    listChats = list;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = listChats;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                listChats = (ArrayList<Chat>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public void updateData(ArrayList<Chat> listChats) {
        listChats.clear();
        listChats.addAll(listChats);
        notifyDataSetChanged();
    };
// ...
}
