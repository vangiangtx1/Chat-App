package com.example.chatapp.Models;

public class Friend {
    private String profilePic, userName, email, describe, gender, friendID;

    public Friend(String profilePic, String userName, String email, String describe, String gender, String friendID) {
        this.profilePic = profilePic;
        this.userName = userName;
        this.email = email;
        this.describe = describe;
        this.gender = gender;
        this.friendID = friendID;
    }
    public Friend() {
    }

    public String getFriendID() {
        return friendID;
    }

    public void setFriendID(String friendID) {
        this.friendID = friendID;
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

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
