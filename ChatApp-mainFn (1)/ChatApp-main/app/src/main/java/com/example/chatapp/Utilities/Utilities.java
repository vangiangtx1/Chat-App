package com.example.chatapp.Utilities;

import android.Manifest;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.chatapp.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Utilities {
    public static String getCurrentTime(String pattern) {
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(currentTime);
    }

    public static void updateDataWithPush(DatabaseReference reference, String user01, String user02, Object object01, Object object02) {
        reference.child(user01).child(user02).push().setValue(object01).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                reference.child(user02).child(user01).push().setValue(object02);
            }
        });
    }

    //Upload dữ liệu
    public static void updateDataWithoutNotify(DatabaseReference reference, String user01, String user02, Object object) {
        reference.child(user01).child(user02).setValue(object);
    }

    // Lấy key ID của historyCall
    public static  String getHistoryCallId() {
        DatabaseReference historyCallReference = FirebaseDatabase.getInstance().getReference().child("HistoryCall").push();
        return historyCallReference.getKey();
    }

    // Xét trạng thái hoạt động của user
    public static void statusActivity(String statusActivity) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("statusActivity", statusActivity);
        databaseReference.child(userId).updateChildren(hashMap);
    }

    public static void deleteFCMToken() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        databaseReference.child("fcmToken").removeValue();
    }
}
