package com.ddd.Shipment.services;

import com.ddd.Shipment.mybatis.dao.OrdersMapper;
import com.ddd.Shipment.mybatis.dao.ParcelInfoMapper;
import com.ddd.Shipment.mybatis.model.Address;
import com.ddd.Shipment.mybatis.model.Orders;
import com.ddd.Shipment.mybatis.model.ParcelInfo;
import com.ddd.Shipment.rest.contracts.OrderRequest;
import com.ddd.Shipment.rest.exception.BadRequestException;
import com.ddd.Shipment.rest.exception.NotFoundException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import javax.persistence.criteria.Order;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class OrderService {

    private final OrdersMapper ordersMapper;
    private final ParcelInfoMapper parcelInfoMapper;

    public OrderService() throws IOException {
        SqlSession session = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("MyBatisConfig.xml")).openSession();
        ordersMapper = session.getMapper(OrdersMapper.class);
        parcelInfoMapper = session.getMapper(ParcelInfoMapper.class);
    }

    public List<Orders> fetchOrders(List<Address> addresses) {
        List<Integer> addressIds = addresses.stream().map(Address::getId).collect(Collectors.toList());
        return ordersMapper.selectAll()
                .stream().filter((order) -> (addressIds.contains(order.getSenderAddressId())))
                .collect(Collectors.toList());
    }

    public Orders fetchOrder(String trackingNumber) throws NotFoundException {
        List<Orders> allOrders = ordersMapper.selectAll();
        if(allOrders.stream().filter((ord) -> ord.getTrackingNumber().equals(trackingNumber)).count() == 1) {
            return allOrders.stream().filter((ord) -> ord.getTrackingNumber().equals(trackingNumber))
                    .collect(Collectors.toList()).get(0);
        }
        throw new NotFoundException("No orders found for tracking number: " + trackingNumber);
    }

    public void createOrder(Orders order) {
        ordersMapper.insert(order);
    }

    public ParcelInfo findParcelInfoReference(OrderRequest orderRequest) {
        List<ParcelInfo> allParcelInfo = parcelInfoMapper.selectAll();
        return allParcelInfo.stream()
                .filter((inf) -> (
                        inf.getSpeedType().equals(orderRequest.getParcelInfo().getSpeedType()) &&
                                inf.getSizeType().equals(orderRequest.getParcelInfo().getSizeType()) &&
                                inf.getFragileType().equals(orderRequest.getParcelInfo().getFragileType())))
                .collect(Collectors.toList()).get(0);
    }

    public ParcelInfo fetchParcelInfo(Integer id) {
        return parcelInfoMapper.selectByPrimaryKey(id);
    }

    public String generateNewTrackingNumber() {
        List<Orders> allOrders = ordersMapper.selectAll();
        while (true) {
            String trackingNumber = "LT" + ThreadLocalRandom.current().nextInt(10000000, 100000000) + "SE";
            if (allOrders.stream().noneMatch((ord) -> ord.getTrackingNumber().equals(trackingNumber))) {
                return trackingNumber;
            }
        }
    }
}
