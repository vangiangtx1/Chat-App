package com.example.chatapp.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.example.chatapp.View.ChatActivity;
import com.example.chatapp.View.ViewSingleFriendActivity;
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

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> implements Filterable  {
    Context context;
    ArrayList<User> listFriends;
    ArrayList<User> listFilterContacts;
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseReference;

    public FriendAdapter(Context context, ArrayList<User> listFriends) {
        this.context = context;
        this.listFriends = listFriends;
        this.listFilterContacts = listFriends;
    }
    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(context).inflate(R.layout.item_friend,parent,false);
        return new FriendViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User friend = listFriends.get(position);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        if (friend ==null) {
            return;
        } else {
            Picasso.get().load(friend.getProfilePic()).into(holder.civAvatarItemFriend);
            holder.tvItemFriendName.setText(friend.getUserName());
            holder.tvItemFriendDescribe.setText(friend.getDescribe());

            mDatabaseReference.child(friend.getUserID()).child("statusActivity").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        if (snapshot.getValue().toString().equals("Online")) {
                            holder.ivItemFriendStatusActivity.setImageResource(R.drawable.icon_online);
                        } else {
                            holder.ivItemFriendStatusActivity.setImageResource(R.drawable.icon_offline);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            holder.btnMoreVertFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showMenuPopup(listFriends.get(holder.getAdapterPosition()).getUserID(), holder.itemView.getContext(), holder.btnMoreVertFriend);
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String friendID = listFriends.get(holder.getAdapterPosition()).getUserID();
                    Intent intent = new Intent(holder.itemView.getContext(), ViewSingleFriendActivity.class);
                    intent.putExtra("userID", friendID);
                    context.startActivity(intent);
                }
            });
        }

    }

    private void showMenuPopup(String friendID, Context context, Button btnMoreVertFriend) {
        PopupMenu popupMenu = new PopupMenu(context,btnMoreVertFriend);
        popupMenu.getMenuInflater().inflate(R.menu.menu_friend, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menuFriendViewProfile:
                        Intent intent = new Intent(context, ViewSingleFriendActivity.class);
                        intent.putExtra("userID", friendID);
                        context.startActivity(intent);
                        break;
                    case R.id.menuFriendChat:
                        Intent intentChat = new Intent(context, ChatActivity.class);
                        intentChat.putExtra("userID", friendID);
                        context.startActivity(intentChat);
                        break;
                    case R.id.menuFriendUnFriend:
                        openDialogConfirmUnFriend(Gravity.CENTER, context, friendID);
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void openDialogConfirmUnFriend(int gravity, Context context, String friendID) {
        final Dialog dialog = new Dialog(context);
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
                    DatabaseReference mFriendReference = FirebaseDatabase.getInstance().getReference().child("Friends");
                    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                    mFriendReference.child(mUser.getUid()).child(friendID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    mFriendReference.child(friendID).child(mUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(context, "Hủy kết bạn thành công", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
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


    @Override
    public int getItemCount() {
        return listFriends.size();
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView civAvatarItemFriend;
        public ImageView ivItemFriendStatusActivity;
        public TextView tvItemFriendName, tvItemFriendDescribe;
        public Button btnMoreVertFriend;
        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            civAvatarItemFriend = itemView.findViewById(R.id.civAvatarItemFriend);
            ivItemFriendStatusActivity = itemView.findViewById(R.id.ivItemFriendStatusActivity);
            tvItemFriendName = itemView.findViewById(R.id.tvItemFriendName);
            tvItemFriendDescribe = itemView.findViewById(R.id.tvItemFriendDescribe);
            btnMoreVertFriend = itemView.findViewById(R.id.btnMoreVertFriend);
        }
    }
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String strSearch = charSequence.toString();
                if (strSearch.isEmpty()) {
                    listFriends = listFilterContacts;
                } else {
                    ArrayList<User> list = new ArrayList<>();
                    for (User friend : listFilterContacts) {
                        if (friend.getUserName().toString().toLowerCase().trim().contains(strSearch.toLowerCase().trim())) {
                            list.add(friend);
                        }
                    }
                    listFriends = list;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = listFriends;
                return filterResults;
            }
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                listFriends = (ArrayList<User>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}
