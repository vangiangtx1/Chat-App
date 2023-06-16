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

import com.example.chatapp.Adapter.ContactAdapter;
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

public class ContactFragment extends Fragment {
    public ContactFragment() {
    }

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    ContactAdapter contactAdapter;
    RecyclerView rvListContact;
    SearchView action_search;
    ArrayList<User> listContact = new ArrayList<>();
    List<User> tempUsers = new ArrayList<>();
    DatabaseReference mFriendReference, mUserReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_contact, container, false);
        setControl(mView);
        setEvent();
        return mView;
    }

    private void setControl(View mView) {
        action_search = (SearchView) mView.findViewById(R.id.action_search);
        action_search.clearFocus();
        rvListContact = (RecyclerView) mView.findViewById(R.id.rvListContact);
        mUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        /* Khởi tạo đối tượng Adapter*/
        contactAdapter = new ContactAdapter(getContext(), listContact);
        rvListContact.setAdapter(contactAdapter);

        /* Khởi tạo một LinearLayout và gán vào RecycleView */
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvListContact.setLayoutManager(layoutManager);
    }

    private void setEvent() {

        loadContact();

        // Xử lý sự kiện tìm kiếm
        action_search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                contactAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                contactAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    private void loadContact() {
        mUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tempUsers.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (mUser != null && user != null && !mUser.getEmail().equals(user.getEmail())) {
                        tempUsers.add(user);
                    }
                }
                mFriendReference.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        listUsers.clear();
                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                String friendID = dataSnapshot.getKey();

                                for (int i = 0; i < tempUsers.size(); i++) {
                                    User user = tempUsers.get(i);

                                    if (user.getUserID().equals(friendID)) {
                                        tempUsers.remove(user);
                                        break;
                                    }
                                }
                            }
                        }
                        listContact.clear();
                        listContact.addAll(tempUsers);
                        contactAdapter.notifyDataSetChanged();
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