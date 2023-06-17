package com.example.whatsapp.Models;

public class CallLogs {

    private String callerId, receiverId, callType;
    long callTimeStamp;

    public CallLogs() {}

    public CallLogs(String callerId, String receiverId, long callTimeStamp, String callType) {
        this.callerId = callerId;
        this.receiverId = receiverId;
        this.callType = callType;
        this.callTimeStamp = callTimeStamp;
    }

    public String getCallerId() {
        return callerId;
    }

    public void setCallerId(String callerId) {
        this.callerId = callerId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public long getCallTimeStamp() {
        return callTimeStamp;
    }

    public void setCallTimeStamp(long callTimeStamp) {
        this.callTimeStamp = callTimeStamp;
    }
}
