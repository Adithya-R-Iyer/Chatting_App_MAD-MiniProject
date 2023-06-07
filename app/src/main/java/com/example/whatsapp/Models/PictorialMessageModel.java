package com.example.whatsapp.Models;

public class PictorialMessageModel {
    String uId, message, messageId, userName,messageType,media;
    Long timestamp;

    public PictorialMessageModel(String uId, String message, String messageId, String userName, Long timestamp,String messageType, String media) {
        this.uId = uId;
        this.message = message;
        this.messageId = messageId;
        this.userName = userName;
        this.timestamp = timestamp;
        this.messageType=messageType;
        this.media=media;
    }

    public PictorialMessageModel(String uId, String message, String messageType, String media) {
        this.uId = uId;
        this.message = message;
        this.messageType=messageType;
        this.media=media;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
