package com.ddd.Shipment.rest.contracts;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderRequest extends SessionToken {

    private UserResponse receiver;
    private Integer senderAddressId;
    private AddressResponse receiverAddress;
    private ParcelInfoResponse parcelInfo;
    private String additionalInfo;
}
