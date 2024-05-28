package com.ddd.Shipment.rest.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class Message {
    private LocalDateTime timestamp;
    private String error;
    private int status;
    private String message;
    private String path;
}

