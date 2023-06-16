package com.example.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.Models.Friend;
import com.example.chatapp.Models.HistoryCall;
import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.example.chatapp.Utilities.Utilities;
import com.example.chatapp.View.ChatActivity;
import com.example.chatapp.View.VideoCallOutgoingActivity;
import com.example.chatapp.View.ViewSingleFriendActivity;
import com.example.chatapp.View.VoiceCallOutGoingActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class HistoryCallAdapter extends RecyclerView.Adapter<HistoryCallAdapter.HistoryCallViewHolder> implements Filterable {
    Context context;
    ArrayList<HistoryCall> listHistoryCall;
    ArrayList<HistoryCall> listFilterHistoryCall;

    public HistoryCallAdapter(Context context, ArrayList<HistoryCall> listHistoryCall) {
        this.context = context;
        this.listHistoryCall = listHistoryCall;
        this.listFilterHistoryCall = listHistoryCall;
    }

    @NonNull
    @Override
    public HistoryCallAdapter.HistoryCallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(context).inflate(R.layout.item_history_call,parent,false);
        return new HistoryCallViewHolder(mView);
    }

    @Override
        public void onBindViewHolder(@NonNull HistoryCallAdapter.HistoryCallViewHolder holder, int position) {
            HistoryCall historyCall = listHistoryCall.get(position);
            if (historyCall == null){
                return;
            } else {
                Picasso.get().load(historyCall.getUserAvatarURL()).placeholder(R.drawable.default_avatar).into(holder.civAvatarItemHistoryCall);
                holder.tvItemHistoryCallName.setText(historyCall.getUserName());
                holder.tvCallTime.setText(historyCall.getCallTime().trim());
                if (historyCall.getStatusCall().equals("MakeCall")) {
                    holder.imageViewStatusCall.setImageResource(R.drawable.icon_callmake);
                } else if (historyCall.getStatusCall().equals("ReceiveCall")) {
                    holder.imageViewStatusCall.setImageResource(R.drawable.icon_callreceive);
                } else if (historyCall.getStatusCall().equals("MissedCall")) {
                    holder.imageViewStatusCall.setImageResource(R.drawable.icon_callmissed);
                }
                if (historyCall.getTypeCall().equals("VideoCall")) {
                    holder.imageViewTypeCall.setImageResource(R.drawable.icon_video_cam);
                } else if (historyCall.getTypeCall().equals("VoiceCall")) {
                    holder.imageViewTypeCall.setImageResource(R.drawable.icon_calls);
                }
                holder.btnMoreVertHistoryCall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                       DatabaseReference mHistoryCallReference = FirebaseDatabase.getInstance().getReference().child("HistoryCall");
                        String userID = listHistoryCall.get(holder.getAdapterPosition()).getUserCallID();
                        String avatarURL = listHistoryCall.get(holder.getAdapterPosition()).getUserAvatarURL();
                        String userName = listHistoryCall.get(holder.getAdapterPosition()).getUserName();
                        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                        PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(), holder.btnMoreVertHistoryCall);
                        popupMenu.getMenuInflater().inflate(R.menu.menu_history_call,popupMenu.getMenu());
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                switch (menuItem.getItemId()) {
                                    case R.id.historyCallMenu_viewProfile:
                                        String friendID = listHistoryCall.get(holder.getAdapterPosition()).getUserCallID();
                                        Intent intent = new Intent(holder.itemView.getContext(), ViewSingleFriendActivity.class);
                                        intent.putExtra("userID", friendID);
                                        context.startActivity(intent);
                                        break;
                                    case R.id.historyCallMenu_chat:
                                        Intent intent02 = new Intent(context, ChatActivity.class);
                                        intent02.putExtra("userID", userID);
                                        context.startActivity(intent02);
                                        break;
                                    case R.id.historyCallMenu_voiceCall:
                                        String typeVoiceCall = "VoiceCall", statustypeVoiceCall = "MakeCall", callTimetypeVoiceCall = Utilities.getCurrentTime("dd/MM/yyyy, hh:mm a");
                                        String historyCallID = Utilities.getHistoryCallId();
                                        long timestampVoiceCall = System.currentTimeMillis();
                                        HistoryCall historyCall = new HistoryCall(historyCallID, avatarURL, userID, userName, statustypeVoiceCall, typeVoiceCall, callTimetypeVoiceCall,timestampVoiceCall);
                                        historyCall.updateHistoryCall(mHistoryCallReference, historyCall, mUser.getUid(), historyCallID);
                                        Intent intentVoiceCall = new Intent(holder.itemView.getContext(), VoiceCallOutGoingActivity.class);
                                        intentVoiceCall.putExtra("receiverID", userID);
                                        context.startActivity(intentVoiceCall);
                                        break;
                                    case R.id.historyCallMenu_videoCall:
                                        String typeVideoCall = "VideoCall", statusVideoCall = "MakeCall", callTimeVideoCall = Utilities.getCurrentTime("dd/MM/yyyy, hh:mm a");
                                        String historyCallId = Utilities.getHistoryCallId();
                                        long timestampVideoCall = System.currentTimeMillis();
                                        HistoryCall historyCallVideoCall = new HistoryCall(historyCallId, avatarURL, userID, userName, statusVideoCall, typeVideoCall, callTimeVideoCall, timestampVideoCall);
                                        historyCallVideoCall.updateHistoryCall(mHistoryCallReference, historyCallVideoCall, mUser.getUid(), historyCallId);
                                        Intent intentVideoCall = new Intent(holder.itemView.getContext(), VideoCallOutgoingActivity.class);
                                        intentVideoCall.putExtra("friendID", userID);
                                        context.startActivity(intentVideoCall);
                                        break;
                                    case R.id.historyCallMenu_delete:
                                        DatabaseReference historyCallReference = FirebaseDatabase.getInstance().getReference().child("HistoryCall").child(mUser.getUid());
                                        String historyCallDelete = listHistoryCall.get(holder.getAdapterPosition()).getHistoryCallId();
                                        historyCallReference.child(historyCallDelete).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful())
                                                    Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show();
                                                listHistoryCall.remove(holder.getAdapterPosition());
                                                notifyDataSetChanged();
                                            }
                                        });
                                        break;
                                }
                                return false;
                            }
                        });
                        popupMenu.show();
                    }
                });
            }

        }

    @Override
    public int getItemCount() {
        return listHistoryCall.size();
    }

    public static class HistoryCallViewHolder extends RecyclerView.ViewHolder {

        CircleImageView civAvatarItemHistoryCall;
        TextView tvItemHistoryCallName, tvCallTime;
        ImageView imageViewStatusCall, imageViewTypeCall;
        Button btnMoreVertHistoryCall;
        public HistoryCallViewHolder(@NonNull View itemView) {
            super(itemView);
            civAvatarItemHistoryCall = itemView.findViewById(R.id.civAvatarItemHistoryCall);
            tvItemHistoryCallName = itemView.findViewById(R.id.tvItemHistoryCallName);
            tvCallTime = itemView.findViewById(R.id.tvCallTime);
            imageViewStatusCall = itemView.findViewById(R.id.imageViewStatusCall);
            imageViewTypeCall = itemView.findViewById(R.id.imageViewTypeCall);
            btnMoreVertHistoryCall = itemView.findViewById(R.id.btnMoreVertHistoryCall);
        }
    }
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String strSearch = charSequence.toString();
                if (strSearch.isEmpty()) {
                    listHistoryCall = listFilterHistoryCall;
                } else {
                    ArrayList<HistoryCall> list = new ArrayList<>();
                    for (HistoryCall historyCall : listHistoryCall) {
                        if (historyCall.getUserName().toLowerCase().trim().contains(strSearch.toLowerCase().trim())) {
                            list.add(historyCall);
                        }
                    }
                    listHistoryCall = list;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = listHistoryCall;
                return filterResults;
            }
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                listHistoryCall = (ArrayList<HistoryCall>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}
