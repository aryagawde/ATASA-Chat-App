package com.example.login_logout;

public class MessageSchedulerClass {
    long DateAndTime;
    String message, senderRoom, recieverRoom;


    public MessageSchedulerClass(long dateAndTime, String message) {
        DateAndTime = dateAndTime;
        this.message = message;
    }

    public MessageSchedulerClass() {}
}