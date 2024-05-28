package com.ddd.Shipment;

import com.ddd.Shipment.rest.AddressController;
import com.ddd.Shipment.rest.AuthenticationController;
import com.ddd.Shipment.rest.OrderController;
import com.ddd.Shipment.rest.util.CORSFilter;
import com.ddd.Shipment.rest.util.CustomExceptionMapper;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/api")
public class ShipmentApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(AddressController.class);
        classes.add(AuthenticationController.class);
        classes.add(OrderController.class);
        classes.add(CustomExceptionMapper.class);
        classes.add(CORSFilter.class);
        return classes;
    }
}