package com.example.login_logout;

public class MessageClass {
    String userId, message, messageId, mediaUrl;
    Long timestamp;

    public MessageClass(String userId, String message, Long timestamp) {
        this.userId = userId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public MessageClass(String userId, String message, String mediaUrl) {
        this.userId = userId;
        this.message = message;
        this.mediaUrl = mediaUrl;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public MessageClass(){}

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
