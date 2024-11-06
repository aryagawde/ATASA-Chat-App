package com.example.login_logout;

public class HelperClass {

    String full_name, username, password, userId;
    Boolean status;

    public HelperClass(String password, String username, String full_name, Boolean status, String userId) {
        this.password = password;
        this.username = username;
        this.full_name = full_name;
        this.status = status;
        this.userId = userId;
    }

    public HelperClass(){}

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
