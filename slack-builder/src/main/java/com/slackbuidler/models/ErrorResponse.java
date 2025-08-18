package com.slackbuidler.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse {
    @JsonProperty("error")
    private String error;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("details")
    private Object details;

    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
    }

    public ErrorResponse(String error, String message, Object details) {
        this.error = error;
        this.message = message;
        this.details = details;
    }

    // Getters
    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public Object getDetails() {
        return details;
    }

    // Setters
    public void setError(String error) {
        this.error = error;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDetails(Object details) {
        this.details = details;
    }
} 