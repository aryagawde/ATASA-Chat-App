package com.example.login_logout;

public class HelperClass {

    String full_name, username, password, userId, lastMessage;
    Boolean isLoggedin;

    public HelperClass(String password, String username, String full_name, Boolean isLoggedin, String userId, String lastMessage) {
        this.password = password;
        this.username = username;
        this.full_name = full_name;
        this.isLoggedin = isLoggedin;
        this.userId = userId;
        this.lastMessage = lastMessage;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Boolean getLoggedin() {
        return isLoggedin;
    }

    public void setLoggedin(Boolean loggedin) {
        isLoggedin = loggedin;
    }

    public HelperClass(){

    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
