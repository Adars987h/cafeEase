package com.inn.cafe.dto;

import lombok.Data;

@Data

public class Response {

    private Object data;
    private String message;

    public Response(){

    }
    public Response(Object data){
        this.data = data;
    }
    public Response(String message){
        this.message = message;
    }
    public Response(Object data, String message){
        this.data = data;
        this.message = message;
    }
}
