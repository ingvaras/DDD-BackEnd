package com.ddd.Shipment.services;

import com.ddd.Shipment.mybatis.dao.OrdersMapper;
import com.ddd.Shipment.mybatis.dao.ShipmentEventMapper;
import com.ddd.Shipment.mybatis.model.Orders;
import com.ddd.Shipment.mybatis.model.ShipmentEvent;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ShipmentEventService {

    private final OrdersMapper ordersMapper;
    private final ShipmentEventMapper shipmentEventMapper;

    public ShipmentEventService() throws IOException {
        SqlSession session = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("MyBatisConfig.xml")).openSession();
        ordersMapper = session.getMapper(OrdersMapper.class);
        shipmentEventMapper = session.getMapper(ShipmentEventMapper.class);
    }

    public List<ShipmentEvent> fetchShipmentEvents(Integer orderId) {
        return shipmentEventMapper.selectAll().stream()
                .filter((event) -> event.getOrderId().equals(orderId))
                .collect(Collectors.toList());
    }

    public void createOrderedEvent(String trackingNumber) {
        Orders order = ordersMapper.selectAll().stream().filter((ord) -> ord.getTrackingNumber().equals(trackingNumber))
                .collect(Collectors.toList()).get(0);
        ShipmentEvent event1 = new ShipmentEvent();
        event1.setOrderId(order.getId());
        event1.setDate(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(LocalDateTime.now()));
        event1.setCity("Vilnius");
        event1.setOrderState("Shipment details successfully registered.");
        shipmentEventMapper.insert(event1);
    }
}
