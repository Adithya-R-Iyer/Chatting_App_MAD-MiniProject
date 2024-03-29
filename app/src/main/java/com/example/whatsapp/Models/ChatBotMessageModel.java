package com.example.whatsapp.Models;

public class ChatBotMessageModel {

    String message, messageId, uId;
    Long timestamp;

    public ChatBotMessageModel(String message, String messageId, Long timestamp, String UId) {
        this.message = message;
        this.messageId = messageId;
        this.timestamp = timestamp;
        this.uId = UId;
    }

    public ChatBotMessageModel(String message, String uId) {
        this.message = message;
        this.uId = uId;
    }

    public ChatBotMessageModel() {

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

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
