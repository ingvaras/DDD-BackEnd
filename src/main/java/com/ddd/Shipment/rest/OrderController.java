package com.ddd.Shipment.rest;

import com.ddd.Shipment.mybatis.dao.AddressMapper;
import com.ddd.Shipment.mybatis.dao.OrdersMapper;
import com.ddd.Shipment.mybatis.dao.ParcelInfoMapper;
import com.ddd.Shipment.mybatis.model.Address;
import com.ddd.Shipment.mybatis.model.Orders;
import com.ddd.Shipment.mybatis.model.ParcelInfo;
import com.ddd.Shipment.mybatis.model.User;
import com.ddd.Shipment.rest.contracts.*;
import com.ddd.Shipment.services.SessionManager;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@Path("/")
public class OrderController {

    private final AddressMapper addressMapper;
    private final OrdersMapper ordersMapper;
    private final ParcelInfoMapper parcelInfoMapper;
    private final SessionManager sessionManager;

    public OrderController() throws IOException {
        SqlSession session = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("MyBatisConfig.xml")).openSession();
        sessionManager = new SessionManager();
        addressMapper = session.getMapper(AddressMapper.class);
        ordersMapper = session.getMapper(OrdersMapper.class);
        parcelInfoMapper = session.getMapper(ParcelInfoMapper.class);
    }


    @Path("/order")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchOrders(SessionToken sessionToken) {
        User user = sessionManager.authenticate(sessionToken);
        if(user != null) {
            List<Integer> addressIds = addressMapper.selectAll()
                    .stream().filter((address) -> (address.getUserId().equals(user.getId())))
                    .map(Address::getId)
                    .collect(Collectors.toList());
            List<Orders> orders = ordersMapper.selectAll()
                    .stream().filter((order) -> (addressIds.contains(order.getSenderAddressId())))
                    .collect(Collectors.toList());
            List<OrderResponse> response = new LinkedList<>();
            for(Orders order : orders) {
                Address sendAdd = addressMapper.selectByPrimaryKey(order.getSenderAddressId());
                ParcelInfo parcelInfo = parcelInfoMapper.selectByPrimaryKey(order.getParcelInfoId());

                UserResponse sender = new UserResponse(user.getName(), user.getEmail(), user.getNumber());
                UserResponse receiver = new UserResponse(order.getReceiverName(), order.getReceiverEmail(), order.getReceiverNumber());
                AddressResponse senderAddress = new AddressResponse(null, sendAdd.getCity(), sendAdd.getAddress(), sendAdd.getPostalCode());
                AddressResponse receiverAddress = new AddressResponse(null, order.getReceiverCity(), order.getReceiverAddress(), order.getReceiverPostalCode());
                ParcelInfoResponse parcelInfoResponse = new ParcelInfoResponse(parcelInfo.getSpeedType(), parcelInfo.getSizeType(), parcelInfo.getFragileType());
                response.add(new OrderResponse(order.getId(), sender, receiver, senderAddress, receiverAddress, parcelInfoResponse, order.getTrackingNumber(), order.getAdditionalInfo()));
            }
            return Response.ok(response).build();
        }
        return Response.status(401).entity("User unauthorized for this action").build();
    }
}
