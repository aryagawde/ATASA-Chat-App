package com.example.login_logout;

public class MessageClass {
    String userId, message, senderRoom, recieverRoom, mediaUrl, messageType, senderMessageId, recieverMessageId;

    Long timestamp;


    public MessageClass(String userId, String message, String messageType, String mediaUrl, String senderRoom, String recieverRoom, String senderMessageId, String recieverMessageId) {
        this.userId = userId;
        this.message = message;
        this.mediaUrl = mediaUrl;
        this.messageType = messageType;
        this.senderRoom = senderRoom;
        this.recieverRoom = recieverRoom;
        this.senderMessageId = senderMessageId;
        this.recieverMessageId = recieverMessageId;
    }

    public MessageClass(){}

    public String getSenderMessageId() {
        return senderMessageId;
    }

    public void setSenderMessageId(String senderMessageId) {
        this.senderMessageId = senderMessageId;
    }

    public String getRecieverMessageId() {
        return recieverMessageId;
    }

    public void setRecieverMessageId(String recieverMessageId) {
        this.recieverMessageId = recieverMessageId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }


    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderRoom() {
        return senderRoom;
    }

    public void setSenderRoom(String senderRoom) {
        this.senderRoom = senderRoom;
    }

    public String getRecieverRoom() {
        return recieverRoom;
    }

    public void setRecieverRoom(String recieverRoom) {
        this.recieverRoom = recieverRoom;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
