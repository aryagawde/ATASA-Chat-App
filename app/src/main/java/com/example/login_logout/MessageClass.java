package com.example.login_logout;

public class MessageClass {
    String userId, message, messageId, mediaUrl, messageType;

    Long timestamp;


    public MessageClass(String userId, String message, String messageType, String mediaUrl) {
        this.userId = userId;
        this.message = message;
        this.mediaUrl = mediaUrl;
        this.messageType = messageType;
    }

    public MessageClass(){}

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

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
