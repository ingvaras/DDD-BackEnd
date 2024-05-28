package com.ddd.Shipment.services;

import com.ddd.Shipment.mybatis.dao.AddressMapper;
import com.ddd.Shipment.mybatis.model.Address;
import com.ddd.Shipment.mybatis.model.User;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class AddressService {

    private final AddressMapper addressMapper;

    public AddressService() throws IOException {
        SqlSession session = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("MyBatisConfig.xml")).openSession();
        addressMapper = session.getMapper(AddressMapper.class);
    }

    public List<Address> fetchAddresses(User user) {
        return addressMapper.selectAll()
                .stream().filter((address) -> (address.getUserId().equals(user.getId())))
                .collect(Collectors.toList());
    }

    public Address fetchAddress(Integer addressId) {
        return addressMapper.selectByPrimaryKey(addressId);
    }

    public void createAddress(Address address) {
        addressMapper.insert(address);
    }

    public void deleteAddress(Integer id) {
        addressMapper.deleteByPrimaryKey(id);
    }
}
