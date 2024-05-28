package com.ddd.Shipment.interceptor;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@Provider
public class MethodLogger implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        Logger logger = Logger.getLogger("UserActivityLog");
        FileHandler fh;
        try {
            fh = new FileHandler("log.log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            logger.info("User did request: " + containerRequestContext.getRequest());
            logger.info("User did method: " + containerRequestContext.getMethod());

        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
    }

}
