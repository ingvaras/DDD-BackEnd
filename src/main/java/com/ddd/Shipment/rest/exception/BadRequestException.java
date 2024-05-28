package com.ddd.Shipment.rest.exception;

import javax.ws.rs.core.Response;

public class BadRequestException extends Http4xxException{

    public BadRequestException(String message) {
        super(message, Response.Status.BAD_REQUEST);
    }
}
