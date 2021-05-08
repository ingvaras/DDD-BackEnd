package com.ddd.Shipment.rest.contracts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ParcelInfoResponse {
    private String speedType;
    private String sizeType;
    private Short fragileType;
}
