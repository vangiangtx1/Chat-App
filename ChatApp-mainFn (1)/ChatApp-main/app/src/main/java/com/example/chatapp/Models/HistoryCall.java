package com.example.chatapp.Models;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;

public class HistoryCall {

    String historyCallId, userAvatarURL, userCallID, userName, statusCall, typeCall, callTime;
    long timestamp;

    public HistoryCall() {
    }

    public HistoryCall(String historyCallId, String userAvatarURL, String userCallID, String userName, String statusCall, String typeCall, String callTime, long timestamp) {
        this.historyCallId = historyCallId;
        this.userAvatarURL = userAvatarURL;
        this.userCallID = userCallID;
        this.userName = userName;
        this.statusCall = statusCall;
        this.typeCall = typeCall;
        this.callTime = callTime;
        this.timestamp = timestamp;

    }

    public String getHistoryCallId() {
        return historyCallId;
    }

    public void setHistoryCallId(String historyCallId) {
        this.historyCallId = historyCallId;
    }

    public String getUserAvatarURL() {
        return userAvatarURL;
    }

    public void setUserAvatarURL(String userAvatarURL) {
        this.userAvatarURL = userAvatarURL;
    }

    public String getUserCallID() {
        return userCallID;
    }

    public void setUserCallID(String userCallID) {
        this.userCallID = userCallID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatusCall() {
        return statusCall;
    }

    public void setStatusCall(String statusCall) {
        this.statusCall = statusCall;
    }

    public String getTypeCall() {
        return typeCall;
    }

    public void setTypeCall(String typeCall) {
        this.typeCall = typeCall;
    }

    public String getCallTime() {
        return callTime;
    }

    public void setCallTime(String callTime) {
        this.callTime = callTime;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void updateHistoryCall(DatabaseReference reference, HistoryCall value, String id, String keyId) {
        DatabaseReference historyCallReference = FirebaseDatabase.getInstance().getReference().child("HistoryCall");
        reference.child(id).child(keyId).setValue(value);
    }
}
