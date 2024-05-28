package com.ddd.Shipment.services;

import com.ddd.Shipment.mybatis.dao.UserMapper;
import com.ddd.Shipment.mybatis.model.User;
import com.ddd.Shipment.rest.exception.BadRequestException;
import com.ddd.Shipment.rest.exception.Http4xxException;
import org.apache.commons.codec.binary.Hex;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

public class UserService {

    private final UserMapper userMapper;

    public UserService() throws IOException {
        SqlSession session = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("MyBatisConfig.xml")).openSession();
        userMapper = session.getMapper(UserMapper.class);
    }

    public void register(User user) throws Http4xxException {
        List<User> users = userMapper.selectAll();
        if (users.stream()
                .map(User::getUsername)
                .collect(Collectors.toList())
                .contains(user.getUsername())) {
            throw new BadRequestException("Username already registered");
        } else if (users.stream()
                .map(User::getEmail)
                .collect(Collectors.toList())
                .contains(user.getEmail())) {
            throw new BadRequestException("Email already registered");
        } else if (users.stream()
                .map(User::getNumber)
                .collect(Collectors.toList())
                .contains(user.getNumber())) {
            throw new BadRequestException("Phone number already registered");
        }
        userMapper.insert(user);
    }

    public String login(User user) throws BadRequestException, NoSuchAlgorithmException {
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
            return sessionToken;
        }
        throw new BadRequestException("Bad credentials");
    }

    public void logout(User user) {
        user.setSessionToken("");
        userMapper.updateByPrimaryKey(user);
    }
}
