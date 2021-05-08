package com.ddd.Shipment.rest.contracts;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddressRequest extends SessionToken {
    private String city;
    private String address;
    private String postalCode;
}
