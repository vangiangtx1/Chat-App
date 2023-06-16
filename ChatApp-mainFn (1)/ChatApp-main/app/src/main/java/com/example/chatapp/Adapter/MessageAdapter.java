package com.example.chatapp.Adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.ViewHolder {
    public CircleImageView civAvatarUserOne;
    public TextView tvSmsUserOne, tvTimeMessageUserOne, tvSmsUserTwo, tvTimeMessageUserTwo, tvTimeImageUserOne, tvTimeImageUserTwo;
    public ImageView ivImageLeft, ivImageRight;
    public MessageAdapter(@NonNull View itemView) {
        super(itemView);
        civAvatarUserOne =  itemView.findViewById(R.id.civAvatarUserOne);
        tvSmsUserOne =  itemView.findViewById(R.id.tvSmsUserOne);
        tvTimeMessageUserOne =  itemView.findViewById(R.id.tvTimeMessageUserOne);
        tvSmsUserTwo =  itemView.findViewById(R.id.tvSmsUserTwo);
        tvTimeMessageUserTwo =  itemView.findViewById(R.id.tvTimeMessageUserTwo);
        ivImageLeft =  itemView.findViewById(R.id.ivImageLeft);
        tvTimeImageUserOne =  itemView.findViewById(R.id.tvTimeImageUserOne);
        ivImageRight =  itemView.findViewById(R.id.ivImageRight);
        tvTimeImageUserTwo =  itemView.findViewById(R.id.tvTimeImageUserTwo);
    }
}
