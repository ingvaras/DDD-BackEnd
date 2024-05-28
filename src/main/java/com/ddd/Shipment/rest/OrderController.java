package com.ddd.Shipment.rest;

import com.ddd.Shipment.mybatis.model.*;
import com.ddd.Shipment.rest.contracts.*;
import com.ddd.Shipment.rest.exception.NotFoundException;
import com.ddd.Shipment.rest.exception.UserUnauthorizedException;
import com.ddd.Shipment.services.AddressService;
import com.ddd.Shipment.services.OrderService;
import com.ddd.Shipment.services.SessionManager;
import com.ddd.Shipment.services.ShipmentEventService;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@Path("/")
public class OrderController {

    private final SessionManager sessionManager;
    private final AddressService addressService;
    private final OrderService orderService;
    private final ShipmentEventService shipmentEventService;

    public OrderController() throws IOException {
        sessionManager = new SessionManager();
        addressService = new AddressService();
        orderService = new OrderService();
        shipmentEventService = new ShipmentEventService();
    }

    @Path("/order")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchOrders(SessionToken sessionToken) throws UserUnauthorizedException {
        User user = sessionManager.authenticate(sessionToken);

        List<Address> addresses = addressService.fetchAddresses(user);
        List<Orders> orders = orderService.fetchOrders(addresses);

        List<OrderResponse> response = new LinkedList<>();
        for(Orders order : orders) {
            Address sendAdd = addressService.fetchAddress(order.getSenderAddressId());
            ParcelInfo parcelInfo = orderService.fetchParcelInfo(order.getParcelInfoId());

            UserResponse sender = new UserResponse(user.getName(), user.getEmail(), user.getNumber());
            UserResponse receiver = new UserResponse(order.getReceiverName(), order.getReceiverEmail(), order.getReceiverNumber());
            AddressResponse senderAddress = new AddressResponse(null, sendAdd.getCity(), sendAdd.getAddress(), sendAdd.getPostalCode());
            AddressResponse receiverAddress = new AddressResponse(null, order.getReceiverCity(), order.getReceiverAddress(), order.getReceiverPostalCode());
            ParcelInfoResponse parcelInfoResponse = new ParcelInfoResponse(parcelInfo.getSpeedType(), parcelInfo.getSizeType(), parcelInfo.getFragileType());
            response.add(new OrderResponse(order.getId(), sender, receiver, senderAddress, receiverAddress, parcelInfoResponse, order.getTrackingNumber(), order.getAdditionalInfo()));
        }
        return Response.ok(response).build();
    }

    @Path("/order")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createOrder(OrderRequest orderRequest) throws UserUnauthorizedException {
        sessionManager.authenticate(new SessionToken(orderRequest.getSessionToken()));

        Orders newOrder = new Orders();
        newOrder.setSenderAddressId(orderRequest.getSenderAddressId());
        newOrder.setReceiverName(orderRequest.getReceiver().getName());
        newOrder.setReceiverEmail(orderRequest.getReceiver().getEmail());
        newOrder.setReceiverNumber(orderRequest.getReceiver().getPhoneNumber());
        newOrder.setReceiverCity(orderRequest.getReceiverAddress().getCity());
        newOrder.setReceiverAddress(orderRequest.getReceiverAddress().getAddress());
        newOrder.setReceiverPostalCode(orderRequest.getReceiverAddress().getPostalCode());
        newOrder.setAdditionalInfo(orderRequest.getAdditionalInfo());
        newOrder.setTrackingNumber(orderService.generateNewTrackingNumber());
        newOrder.setParcelInfoId(orderService.findParcelInfoReference(orderRequest).getId());

        orderService.createOrder(newOrder);

        shipmentEventService.createOrderedEvent(newOrder.getTrackingNumber());

        return Response.ok(newOrder.getTrackingNumber()).build();
    }

    @Path("/track/{trackingNumber}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchOrders(@PathParam("trackingNumber") String trackingNumber) throws NotFoundException {

        Integer orderId = orderService.fetchOrder(trackingNumber).getId();
        List<ShipmentEvent> shipmentEvents = shipmentEventService.fetchShipmentEvents(orderId);

           List<ShipmentEventResponse> response = shipmentEvents.stream()
                   .map((event) -> new ShipmentEventResponse(event.getOrderState(), event.getCity(), event.getDate()))
                   .collect(Collectors.toList());
           return Response.ok(response).build();
    }
}
