package com.example.chatapp.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.chatapp.Adapter.HistoryCallAdapter;
import com.example.chatapp.Models.Chat;
import com.example.chatapp.Models.HistoryCall;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class CallFragment extends Fragment {
    RecyclerView rvListCallHistory;
    SearchView action_searchCall;
    Button btnDeleteAllCall;
    ArrayList<HistoryCall> listCallHistory = new ArrayList<>();
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    HistoryCallAdapter historyCallAdapter;

    DatabaseReference mHistoryCallReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_call, container, false);
        setControl(mView);
        setEvent();
        return mView;
    }

    private void setControl(View mView) {
        action_searchCall = (SearchView) mView.findViewById(R.id.action_searchCall);
        btnDeleteAllCall = (Button) mView.findViewById(R.id.btnDeleteAllCall);
        rvListCallHistory = (RecyclerView) mView.findViewById(R.id.rvListCallHistory);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mHistoryCallReference = FirebaseDatabase.getInstance().getReference().child("HistoryCall");

        /* Khởi tạo đối tượng Adapter*/
        historyCallAdapter = new HistoryCallAdapter(getContext(), listCallHistory);
        rvListCallHistory.setAdapter(historyCallAdapter);

        /* Khởi tạo một LinearLayout và gán vào RecycleView */
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvListCallHistory.setLayoutManager(layoutManager);
    }

    private void setEvent() {
        loadHistoryCall();
        btnDeleteAllCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialogConfirmDeleteCall(Gravity.CENTER);
            }
        });

        action_searchCall.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                historyCallAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                historyCallAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    private void loadHistoryCall() {
        mHistoryCallReference.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listCallHistory.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        HistoryCall historyCall = dataSnapshot.getValue(HistoryCall.class);
                        historyCall.setHistoryCallId(dataSnapshot.getKey());
                        listCallHistory.add(historyCall);
                    }
                    Collections.sort(listCallHistory, new Comparator<HistoryCall>() {
                        @Override
                        public int compare(HistoryCall historyCall, HistoryCall historyCall02) {
                            return Long.compare(historyCall02.getTimestamp(), historyCall.getTimestamp());
                        }
                    });
                    historyCallAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void openDialogConfirmDeleteCall(int gravity) {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_delete_all_historycall);
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
            Button btnConfirmDeleteCall = dialog.findViewById(R.id.btnConfirmDeleteCall);
            Button btnCancelConfirmDeleteCall = dialog.findViewById(R.id.btnCancelConfirmDeleteCall);

            btnConfirmDeleteCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mHistoryCallReference.child(mUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getContext(), "Đã xóa tất cả các cuộc trò chuyện", Toast.LENGTH_SHORT).show();
                            listCallHistory.clear();
                            historyCallAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    });
                }
            });
            btnCancelConfirmDeleteCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
        }
        dialog.show();
    }

}