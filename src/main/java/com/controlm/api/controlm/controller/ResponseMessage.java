package com.controlm.api.controlm.controller;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.List;

public class ResponseMessage implements Serializable {
    private String messsage;
    private List<ResponseErrorMessage> errors;

    public String getMesssage() {
        return messsage;
    }

    public void setMesssage(String messsage) {
        this.messsage = messsage;
    }

    public List<ResponseErrorMessage> getErrors() {
        return errors;
    }

    public void setErrors(List<ResponseErrorMessage> errors) {
        this.errors = errors;
    }
}
