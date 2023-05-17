package com.example.whatsapp.Models;

public class MessagesModel {

    String uId, message, messageId, userName;
    Long timestamp;

    public MessagesModel(String uId, String message, String messageId, String userName, Long timestamp) {
        this.uId = uId;
        this.message = message;
        this.messageId = messageId;
        this.userName = userName;
        this.timestamp = timestamp;
    }

    public MessagesModel(String uId, String message) {
        this.uId = uId;
        this.message = message;
    }

    public MessagesModel() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getuId() {
        return uId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
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

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
