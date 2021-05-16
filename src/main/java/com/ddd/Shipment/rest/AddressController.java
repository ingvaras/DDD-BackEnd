package com.ddd.Shipment.rest;

import com.ddd.Shipment.mybatis.dao.AddressMapper;
import com.ddd.Shipment.mybatis.model.Address;
import com.ddd.Shipment.mybatis.model.User;
import com.ddd.Shipment.rest.contracts.AddressRequest;
import com.ddd.Shipment.rest.contracts.AddressResponse;
import com.ddd.Shipment.rest.contracts.SessionToken;
import com.ddd.Shipment.services.SessionManager;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

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

    private final AddressMapper addressMapper;
    private final SessionManager sessionManager;

    public AddressController() throws IOException {
        SqlSession session = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("MyBatisConfig.xml")).openSession();
        addressMapper = session.getMapper(AddressMapper.class);
        sessionManager = new SessionManager();
    }

    @Path("/address")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchAddresses(SessionToken sessionToken) {
        User user = sessionManager.authenticate(sessionToken);
        if(user != null) {
            List<Address> addresses = addressMapper.selectAll()
                    .stream().filter((address) -> (address.getUserId().equals(user.getId())))
                    .collect(Collectors.toList());
            List<AddressResponse> addressesResponse = addresses
                    .stream().map((address) -> new AddressResponse(address.getId(), address.getCity(), address.getAddress(), address.getPostalCode()))
                    .collect(Collectors.toList());
            return Response.ok(addressesResponse).build();
        }
        return Response.status(401).entity("User unauthorized for this action").build();
    }


    @Path("/address")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addAddress(AddressRequest request) {
        User user = sessionManager.authenticate(new SessionToken(request.getSessionToken()));
        if(user != null) {
            Address newAddress = new Address();
            newAddress.setCity(request.getCity());
            newAddress.setAddress(request.getAddress());
            newAddress.setPostalCode(request.getPostalCode());
            newAddress.setUserId(user.getId());
            addressMapper.insert(newAddress);
            return Response.status(201).build();
        }
        return Response.status(401).entity("User unauthorized for this action").build();
    }

    @Path("/address/{id}")
    @DELETE
    @Consumes
    public Response addAddress(@PathParam("id") Integer id, SessionToken sessionToken) {
        User user = sessionManager.authenticate(sessionToken);
        if(user != null) {
            addressMapper.deleteByPrimaryKey(id);
            return Response.status(204).build();
        }
        return Response.status(401).entity("User unauthorized for this action").build();
    }
}
