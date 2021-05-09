package com.ddd.Shipment.rest;

import com.ddd.Shipment.mybatis.dao.AddressMapper;
import com.ddd.Shipment.mybatis.dao.OrdersMapper;
import com.ddd.Shipment.mybatis.dao.ParcelInfoMapper;
import com.ddd.Shipment.mybatis.dao.ShipmentEventMapper;
import com.ddd.Shipment.mybatis.model.*;
import com.ddd.Shipment.rest.contracts.*;
import com.ddd.Shipment.services.SessionManager;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.criteria.Order;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@ApplicationScoped
@Path("/")
public class OrderController {

    private final AddressMapper addressMapper;
    private final OrdersMapper ordersMapper;
    private final ParcelInfoMapper parcelInfoMapper;
    private final ShipmentEventMapper shipmentEventMapper;
    private final SessionManager sessionManager;

    public OrderController() throws IOException {
        SqlSession session = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("MyBatisConfig.xml")).openSession();
        sessionManager = new SessionManager();
        addressMapper = session.getMapper(AddressMapper.class);
        ordersMapper = session.getMapper(OrdersMapper.class);
        shipmentEventMapper = session.getMapper(ShipmentEventMapper.class);
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

    @Path("/order")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createOrder(OrderRequest orderRequest) {
        User user = sessionManager.authenticate(new SessionToken(orderRequest.getSessionToken()));
        if(user != null) {
            try {
                Orders newOrder = new Orders();
                newOrder.setSenderAddressId(orderRequest.getSenderAddressId());
                newOrder.setReceiverName(orderRequest.getReceiver().getName());
                newOrder.setReceiverEmail(orderRequest.getReceiver().getEmail());
                newOrder.setReceiverNumber(orderRequest.getReceiver().getPhoneNumber());
                newOrder.setReceiverCity(orderRequest.getReceiverAddress().getCity());
                newOrder.setReceiverAddress(orderRequest.getReceiverAddress().getAddress());
                newOrder.setReceiverPostalCode(orderRequest.getReceiverAddress().getPostalCode());
                newOrder.setAdditionalInfo(orderRequest.getAdditionalInfo());

                List<Orders> allOrders = ordersMapper.selectAll();
                while (true) {
                    String trackingNumber = "LT" + ThreadLocalRandom.current().nextInt(10000000, 100000000) + "SE";
                    if (allOrders.stream().noneMatch((ord) -> ord.getTrackingNumber().equals(trackingNumber))) {
                        newOrder.setTrackingNumber(trackingNumber);
                        break;
                    }
                }
                List<ParcelInfo> allParcelInfo = parcelInfoMapper.selectAll();
                Integer parcelInfoId = allParcelInfo.stream()
                        .filter((inf) -> (
                                inf.getSpeedType().equals(orderRequest.getParcelInfo().getSpeedType()) &&
                                        inf.getSizeType().equals(orderRequest.getParcelInfo().getSizeType()) &&
                                        inf.getFragileType().equals(orderRequest.getParcelInfo().getFragileType())))
                        .collect(Collectors.toList()).get(0).getId();
                newOrder.setParcelInfoId(parcelInfoId);

                ordersMapper.insert(newOrder);

                return Response.ok(newOrder.getTrackingNumber()).build();
            } catch (Exception e) {
                return Response.status(500, "Internal Server Fault").build();
            }
        }
        return Response.status(401).entity("User unauthorized for this action").build();
    }

    @Path("/track/{trackingNumber}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchOrders(@PathParam("trackingNumber") String trackingNumber) {

           List<Orders> allOrders = ordersMapper.selectAll();
           if(allOrders.stream().filter((ord) -> ord.getTrackingNumber().equals(trackingNumber)).count() == 1) {
               try {
                   Orders orderToBeTracked = allOrders.stream().filter((ord) -> ord.getTrackingNumber().equals(trackingNumber))
                           .collect(Collectors.toList()).get(0);
                   Integer orderId = orderToBeTracked.getId();
                   List<ShipmentEvent> shipmentEvents = shipmentEventMapper.selectAll().stream()
                           .filter((event) -> event.getOrderId().equals(orderId))
                           .collect(Collectors.toList());
                   List<ShipmentEventResponse> response = shipmentEvents.stream()
                           .map((event) -> new ShipmentEventResponse(event.getOrderState(), event.getCity(), event.getDate()))
                           .collect(Collectors.toList());
                   return Response.ok(response).build();
               } catch (Exception e) {
                   return Response.status(500, "Internal Server Fault").build();
               }
           } else {
               return Response.status(404, "Order not found").build();
           }
    }
}
