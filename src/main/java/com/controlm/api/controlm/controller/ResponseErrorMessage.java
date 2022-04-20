package com.controlm.api.controlm.controller;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class ResponseErrorMessage implements Serializable {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
