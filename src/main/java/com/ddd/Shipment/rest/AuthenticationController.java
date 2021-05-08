package com.ddd.Shipment.rest;

import com.ddd.Shipment.mybatis.dao.UserMapper;
import com.ddd.Shipment.mybatis.model.User;
import com.ddd.Shipment.rest.contracts.SessionToken;
import com.ddd.Shipment.services.SessionManager;
import org.apache.commons.codec.binary.Hex;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@Path("/")
public class AuthenticationController {

    private final UserMapper userMapper;
    private final SessionManager sessionManager;

    public AuthenticationController() throws IOException {
        SqlSession session = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("MyBatisConfig.xml")).openSession();
        userMapper = session.getMapper(UserMapper.class);
        sessionManager = new SessionManager();
    }

    @Path("/register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(User user) {
        List<User> users = userMapper.selectAll();
        if(users.stream()
                .map(User::getUsername)
                .collect(Collectors.toList())
                .contains(user.getUsername())) {
            return Response.status(400).entity( "Username already registered").build();
        } else if(users.stream()
                .map(User::getEmail)
                .collect(Collectors.toList())
                .contains(user.getEmail())) {
            return Response.status(400).entity( "Email already registered").build();
        } else if(users.stream()
                .map(User::getNumber)
                .collect(Collectors.toList())
                .contains(user.getNumber())) {
            return Response.status(400).entity("Phone number already registered").build();
        }

        userMapper.insert(user);
        return Response.status(Response.Status.CREATED).build();
    }

    @Path("/login")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(User user) throws NoSuchAlgorithmException {
        List<User> users = userMapper.selectAll();
        if(users.stream()
                .filter((usr) -> (usr.getUsername().equals(user.getUsername()) && usr.getPassword().equals(user.getPassword())))
                .count() == 1) {
            User dbUser = users.stream()
                    .filter((usr) -> (usr.getUsername().equals(user.getUsername()) && usr.getPassword().equals(user.getPassword())))
                    .collect(Collectors.toList())
                    .get(0);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String secretToken = user.getUsername() + user.getPassword() + System.currentTimeMillis();
            String sessionToken = String.valueOf(Hex.encodeHex(digest.digest(secretToken.getBytes(StandardCharsets.UTF_8))));
            dbUser.setSessionToken(sessionToken);
            userMapper.updateByPrimaryKey(dbUser);
            return Response.ok(sessionToken).build();
        }
        return Response.status(401).entity("Failed to authenticate").build();
    }

    @Path("/logout")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response logout(SessionToken sessionToken) {
        User dbUser = sessionManager.authenticate(sessionToken);
        if(dbUser != null) {
            dbUser.setSessionToken("");
            userMapper.updateByPrimaryKey(dbUser);
            return Response.ok().build();
        }
        return Response.status(400).build();
    }


}
