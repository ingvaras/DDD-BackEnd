package com.ddd.Shipment.rest.contracts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class OrderResponse {

    private Integer id;
    private UserResponse sender;
    private UserResponse receiver;
    private AddressResponse senderAddress;
    private AddressResponse receiverAddress;
    private ParcelInfoResponse parcelInfo;
    private String trackingNumber;
    private String additionalInfo;

}
