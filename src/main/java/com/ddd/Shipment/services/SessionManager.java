package com.ddd.Shipment.services;

import com.ddd.Shipment.mybatis.dao.UserMapper;
import com.ddd.Shipment.mybatis.model.User;
import com.ddd.Shipment.rest.contracts.SessionToken;
import com.ddd.Shipment.rest.exception.UserUnauthorizedException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class SessionManager {

    private final UserMapper userMapper;

    public SessionManager() throws IOException {
        SqlSession session = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("MyBatisConfig.xml")).openSession();
        userMapper = session.getMapper(UserMapper.class);
    }

    public User authenticate(SessionToken sessionToken) throws UserUnauthorizedException {
        List<User> users = userMapper.selectAll();
        if (users.stream()
                .filter((usr) -> (sessionToken.getSessionToken().equals(usr.getSessionToken())))
                .count() == 1) {
            return users.stream()
                    .filter((usr) -> (sessionToken.getSessionToken().equals(usr.getSessionToken())))
                    .collect(Collectors.toList())
                    .get(0);
        } else {
            throw new UserUnauthorizedException();
        }
    }

}
