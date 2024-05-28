package com.ddd.Shipment.rest.util;
import com.ddd.Shipment.rest.exception.Http4xxException;
import com.ddd.Shipment.rest.exception.Message;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Provider
public class CustomExceptionMapper implements ExceptionMapper<Http4xxException> {

    @Context
    private HttpServletRequest request;

    @Override
    public Response toResponse(Http4xxException exception) {
        Message message = Message.builder()
                .timestamp(LocalDateTime.now())
                .error(exception.getStatus().getReasonPhrase())
                .status(exception.getStatus().getStatusCode())
                .message(exception.getMessage())
                .path(request.getRequestURI())
                .build();

        return Response.status(exception.getStatus())
                .entity(message)
                .build();
    }
}
