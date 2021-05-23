package com.sudip.chatify.Models;

public class User {
     private String uid, name, emailAddress, profileImage;

     public User(){

     }

    public User(String uid, String name, String emailAddress, String profileImage) {
        this.uid = uid;
        this.name = name;
        this.emailAddress = emailAddress;
        this.profileImage = profileImage;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
