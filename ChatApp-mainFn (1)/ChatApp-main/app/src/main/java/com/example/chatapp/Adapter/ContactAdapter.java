package com.example.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.example.chatapp.View.ViewItemContactActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> implements Filterable {
        Context context;
        ArrayList<User> listContacts;
        ArrayList<User> listFilterContacts;

    public ContactAdapter(Context context, ArrayList<User> listContacts) {
        this.context = context;
        this.listContacts = listContacts;
        this.listFilterContacts = listContacts;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        User user = listContacts.get(position);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference mStorageReference = storage.getReference().child("profilePic/default_avatar.png");
        if (user != null) {
            if (user != null) {
                if (user.getProfilePic().isEmpty()) {
                    mStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri.toString()).placeholder(R.drawable.default_avatar).into(holder.civAvatarItemContact);
                        }
                    });
                } else {
                    Picasso.get().load(user.getProfilePic()).placeholder(R.drawable.default_avatar).into(holder.civAvatarItemContact);
                }
            }

            holder.tvItemContactName.setText(user.getUserName());
            holder.tvItemContactEmail.setText(user.getEmail());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String userID = listContacts.get(holder.getAdapterPosition()).getUserID();
                    Intent intent = new Intent(holder.itemView.getContext(), ViewItemContactActivity.class);
                    intent.putExtra("userID",userID);
                    context.startActivity(intent);

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listContacts.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        CircleImageView civAvatarItemContact;
        TextView tvItemContactName, tvItemContactEmail;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            civAvatarItemContact = itemView.findViewById(R.id.civAvatarItemContact);
            tvItemContactName = itemView.findViewById(R.id.tvItemContactName);
            tvItemContactEmail = itemView.findViewById(R.id.tvItemContactEmail);
        }
    }


    /* Khởi tọa Filter phục vụ searchView*/
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String strSearch = charSequence.toString();
                if (strSearch.isEmpty()) {
                    listContacts = listFilterContacts;
                } else {
                    ArrayList<User> list = new ArrayList<>();
                    for (User user : listFilterContacts) {
                        if (user.getEmail().toString().toLowerCase().trim().equals(strSearch.toLowerCase().trim())) {
                            list.add(user);
                        }
                    }
                    listContacts = list;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = listContacts;
                return filterResults;
            }
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                listContacts = (ArrayList<User>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}
