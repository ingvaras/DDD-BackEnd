package com.ddd.Shipment.rest.exception;

import lombok.Getter;

import javax.ws.rs.core.Response;

@Getter
public class Http4xxException extends Exception{

    private final Response.Status status;

    public Http4xxException(String message, Response.Status status) {
        super(message);
        this.status = status;
    }
}
