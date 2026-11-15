package com.orderflow.orderflow.dto;

import java.util.List;

public class ErrorResponse {

    private int status;
    private String error;
    private List<String> messages;

    // No-argument constructor
    public ErrorResponse() {
    }

    // Parameterized constructor
    public ErrorResponse(int status, String error, List<String> messages) {
        this.status = status;
        this.error = error;
        this.messages = messages;
    }

    // Getters and Setters
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
