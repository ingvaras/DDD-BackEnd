package com.ddd.Shipment.rest;

import com.ddd.Shipment.mybatis.model.User;
import com.ddd.Shipment.rest.contracts.SessionToken;
import com.ddd.Shipment.rest.exception.BadRequestException;
import com.ddd.Shipment.rest.exception.Http4xxException;
import com.ddd.Shipment.rest.exception.UserUnauthorizedException;
import com.ddd.Shipment.services.SessionManager;
import com.ddd.Shipment.services.UserService;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@ApplicationScoped
@Path("/")
public class AuthenticationController {

    private final SessionManager sessionManager;
    private final UserService userService;

    public AuthenticationController() throws IOException {
        sessionManager = new SessionManager();
        userService = new UserService();
    }

    @Path("/register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(User user) throws Http4xxException {
        userService.register(user);

        return Response.status(Response.Status.CREATED).build();
    }

    @Path("/login")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(User user) throws NoSuchAlgorithmException, BadRequestException {
        String sessionToken = userService.login(user);

        return Response.ok(sessionToken).build();
    }

    @Path("/logout")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(SessionToken sessionToken) throws UserUnauthorizedException {
        User dbUser = sessionManager.authenticate(sessionToken);
        userService.logout(dbUser);
        return Response.noContent().build();
    }
}
