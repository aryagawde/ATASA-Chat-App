package com.example.login_logout;

public class HelperClass {
    String full_name, username, password;

    public HelperClass(String password, String username, String full_name) {
        this.password = password;
        this.username = username;
        this.full_name = full_name;
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
