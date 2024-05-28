package com.ddd.Shipment.rest.exception;

import javax.ws.rs.core.Response;

public class NotFoundException extends Http4xxException {

    public NotFoundException(String message) {
        super(message, Response.Status.NOT_FOUND);
    }
}
