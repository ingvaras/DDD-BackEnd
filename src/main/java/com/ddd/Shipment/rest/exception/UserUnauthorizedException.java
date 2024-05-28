package com.ddd.Shipment.rest.exception;

import javax.ws.rs.core.Response;

public class UserUnauthorizedException extends Http4xxException {

    public UserUnauthorizedException() {
        super("User is not authorized", Response.Status.UNAUTHORIZED);
    }
}
