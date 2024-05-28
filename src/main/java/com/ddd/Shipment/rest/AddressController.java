package com.ddd.Shipment.rest;

import com.ddd.Shipment.mybatis.model.Address;
import com.ddd.Shipment.mybatis.model.User;
import com.ddd.Shipment.rest.contracts.AddressRequest;
import com.ddd.Shipment.rest.contracts.AddressResponse;
import com.ddd.Shipment.rest.contracts.SessionToken;
import com.ddd.Shipment.rest.exception.UserUnauthorizedException;
import com.ddd.Shipment.services.AddressService;
import com.ddd.Shipment.services.SessionManager;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@Path("/")
public class AddressController {

    private final AddressService addressService;
    private final SessionManager sessionManager;

    public AddressController() throws IOException {
        addressService = new AddressService();
        sessionManager = new SessionManager();
    }

    @Path("/address")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchAddresses(SessionToken sessionToken) throws UserUnauthorizedException {
        User user = sessionManager.authenticate(sessionToken);

        List<Address> addresses = addressService.fetchAddresses(user);
        List<AddressResponse> addressesResponse = addresses
                .stream().map((address) -> new AddressResponse(address.getId(), address.getCity(), address.getAddress(), address.getPostalCode()))
                .collect(Collectors.toList());
        return Response.ok(addressesResponse).build();
    }


    @Path("/address")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addAddress(AddressRequest request) throws UserUnauthorizedException {
        User user = sessionManager.authenticate(new SessionToken(request.getSessionToken()));

        Address newAddress = new Address();
        newAddress.setCity(request.getCity());
        newAddress.setAddress(request.getAddress());
        newAddress.setPostalCode(request.getPostalCode());
        newAddress.setUserId(user.getId());

        addressService.createAddress(newAddress);

        return Response.status(201).build();
    }

    @Path("/address/{id}")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAddress(@PathParam("id") Integer id, SessionToken sessionToken) throws UserUnauthorizedException {
        sessionManager.authenticate(sessionToken);

        addressService.deleteAddress(id);
        return Response.status(204).build();
    }
}
