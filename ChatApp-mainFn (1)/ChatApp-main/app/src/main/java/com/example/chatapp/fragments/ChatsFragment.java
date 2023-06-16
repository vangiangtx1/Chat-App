package com.example.chatapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatapp.Adapter.ChatAdapter;
import com.example.chatapp.Models.Chat;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ChatsFragment extends Fragment {
    SearchView action_searchChat;
    RecyclerView rvListChat;
    ChatAdapter chatAdapter;
    ArrayList<Chat> listChat = new ArrayList<>();
    FirebaseAuth mAuth;
    DatabaseReference mChatReference, mUserReference, mRequestReference;
    String friendID;
    FirebaseUser mUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_chats, container, false);
        setControl(mView);
        setEvent();
        return mView;
    }

    private void setControl(View mView) {
        rvListChat = (RecyclerView) mView.findViewById(R.id.rvListChat);
        action_searchChat = mView.findViewById(R.id.action_searchChat);
        mAuth = FirebaseAuth.getInstance();
        mChatReference = FirebaseDatabase.getInstance().getReference().child("Chats");
        mUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mRequestReference = FirebaseDatabase.getInstance().getReference().child("Requests");
        mUser = mAuth.getCurrentUser();

        chatAdapter = new ChatAdapter(getContext(), listChat);
        rvListChat.setAdapter(chatAdapter);
//        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL); /* Tạo ngăn cách giữa 2 đối tượng*/
//        rvListChat.addItemDecoration(itemDecoration);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext()); /* Khởi tạo một LinearLayout và gán vào RecycleView */
        rvListChat.setLayoutManager(layoutManager);
    }

    private void setEvent() {
        loadListChats();
        action_searchChat.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                chatAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                chatAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    private void loadListChats() {
        mChatReference.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listChat.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    mAuth = FirebaseAuth.getInstance();
                    mUser = mAuth.getCurrentUser();
                    chat.setFriendID(dataSnapshot.getKey());
                    listChat.add(chat);
                }
                Collections.sort(listChat, new Comparator<Chat>() {
                    @Override
                    public int compare(Chat chat1, Chat chat2) {
                        return Long.compare(chat2.getTimestamp(), chat1.getTimestamp());
                    }
                });
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}