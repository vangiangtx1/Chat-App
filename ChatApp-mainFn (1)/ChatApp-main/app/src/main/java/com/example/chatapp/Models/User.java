package com.example.chatapp.Models;

public class User {
    String profilePic, userName, email, password, userID,  describe , gender, statusActivity;

    public User() {

    }

    public User(String profilePic, String userName, String email, String password, String userID, String describe, String gender, String statusActivity) {
        this.profilePic = profilePic;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.userID = userID;
        this.describe = describe;
        this.gender = gender;
        this.statusActivity = statusActivity;
    }

    public String getStatusActivity() {
        return statusActivity;
    }

    public void setStatusActivity(String statusActivity) {
        this.statusActivity = statusActivity;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe= describe;
    }
}
