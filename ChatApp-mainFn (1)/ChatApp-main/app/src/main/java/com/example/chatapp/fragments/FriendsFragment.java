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

import com.example.chatapp.Adapter.FriendAdapter;
import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {
    public FriendsFragment() {
    }

    FriendAdapter friendAdapter;
    ArrayList<User> listFriends = new ArrayList<>();
    List<User> tempUsers = new ArrayList<>();
    SearchView action_searchFriend;
    RecyclerView rvListFriend;
    FirebaseAuth mAuth;
    DatabaseReference mFriendReference, mDatabaseReference;
    String friendID;
    FirebaseUser mUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_friends, container, false);
        setControl(mView);
        setEvent();
        return mView;
    }

    private void setControl(View mView) {
        action_searchFriend = mView.findViewById(R.id.action_searchFriend);
        rvListFriend = mView.findViewById(R.id.rvListFriend);
        mAuth = FirebaseAuth.getInstance();
        mFriendReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mUser = mAuth.getCurrentUser();

        friendAdapter = new FriendAdapter(getContext(), listFriends);
        rvListFriend.setAdapter(friendAdapter);
        /* Khởi tạo một LinearLayout và gán vào RecycleView */
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvListFriend.setLayoutManager(layoutManager);
    }

    private void setEvent() {

//        loadFriend();
        loadFriend();
        action_searchFriend.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                friendAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                friendAdapter.getFilter().filter(newText);
                return false;
            }
        });


    }
    private void loadFriend() {
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tempUsers.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (mUser != null && !mUser.getEmail().equals(user.getEmail())) {
                        tempUsers.add(user);
                    }
                }
                mFriendReference.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<User> friendList = new ArrayList<>();
                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                String friendID = dataSnapshot.getKey();

                                for (User user : tempUsers) {
                                    if (user.getUserID().equals(friendID)) {
                                        friendList.add(user);
                                        break;
                                    }
                                }
                            }
                        }
                        listFriends.clear();
                        listFriends.addAll(friendList);
                        friendAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}