package com.example.rent_it.Model;

public class User {
    private  String id;
    private  String userName;
    private  String fullName;
    private String email;
    private  String imageUrl;
    private  String bio;

    public User(String id, String userName, String fullName, String email, String imageUrl, String bio) {
        this.id = id;
        this.userName = userName;
        this.fullName = fullName;
        this.email = email;
        this.imageUrl = imageUrl;
        this.bio = bio;
    }

    public  User(){
    }


    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
