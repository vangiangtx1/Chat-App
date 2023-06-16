package com.example.chatapp.Models;

public class Chat {
    private String userName, profilePic, message, friendID, senderID, receiverID, dateTime, type;
    private long timestamp;

    public Chat() {
    }

    public Chat(String userName, String profilePic, String message, String friendID, String senderID, String receiverID, String dateTime, String type, long timestamp) {
        this.userName = userName;
        this.profilePic = profilePic;
        this.message = message;
        this.friendID = friendID;
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.dateTime = dateTime;
        this.type = type;
        this.timestamp = timestamp;
    }

    public Chat(String userName, String profilePic, String message, String senderID, String receiverID, String dateTime, long timestamp, String type) {
        this.userName = userName;
        this.profilePic = profilePic;
        this.message = message;
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.dateTime = dateTime;
        this.timestamp = timestamp;
        this.type = type;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFriendID() {
        return friendID;
    }

    public void setFriendID(String friendID) {
        this.friendID = friendID;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
