package com.example.whatsapp.Models;

public class Users {
    private String profilepic, userName, mail, password, userId, lastMessage, status,online, incomingVideoCall, deviceId;
    private Boolean isAvailableForCalls;

    public Users(String profilepic, String userName, String mail, String password, String userId, String lastMessage, String status, String online, Boolean isAvailableForCalls, String incomingVideoCall, String deviceId) {
        this.profilepic = profilepic;
        this.userName = userName;
        this.mail = mail;
        this.password = password;
        this.userId = userId;
        this.lastMessage = lastMessage;
        this.status = status;
        this.online=online;
        this.isAvailableForCalls = isAvailableForCalls;
        this.incomingVideoCall = incomingVideoCall;
        this.deviceId = deviceId;
    }

    public Users() {}

    // SignUp Constructor
    public Users(String userName, String mail, String password,String online, Boolean isAvailableForCalls, String incomingVideoCall, String deviceId) {
        this.userName = userName;
        this.mail = mail;
        this.password = password;
        this.online=online;
        this.isAvailableForCalls = isAvailableForCalls;
        this.incomingVideoCall = incomingVideoCall;
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getIncomingVideoCall() {
        return incomingVideoCall;
    }

    public void setIncomingVideoCall(String incomingVideoCall) {
        this.incomingVideoCall = incomingVideoCall;
    }

    public Boolean getAvailableForCalls() {
        return isAvailableForCalls;
    }

    public void setAvailableForCalls(Boolean availableForCalls) {
        isAvailableForCalls = availableForCalls;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfilepic() {
        return profilepic;
    }

    public void setProfilepic(String profilepic) {
        this.profilepic = profilepic;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMail() {
        return mail;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }
}
