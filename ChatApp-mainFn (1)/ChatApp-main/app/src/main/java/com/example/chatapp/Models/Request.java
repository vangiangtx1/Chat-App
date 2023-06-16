package com.example.chatapp.Models;

public class Request {
    String userName, profilePic, userID, status;

    public Request() {
    }

    public Request(String userName, String profilePic, String userID, String status) {
        this.userName = userName;
        this.profilePic = profilePic;
        this.userID = userID;
        this.status = status;
    }

    public String getUserID() {
        return userID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUserID(String userID) {
        this.userID = userID;
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
}
