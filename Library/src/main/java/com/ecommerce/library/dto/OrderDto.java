package com.ecommerce.library.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {

    private Long orderDtoId;
    private Date orderDtoDate;
    private Date orderDtodeliveryDate;
    private String orderDtoStatus;
    private double orderDtotalPrice;
    private int orderDtoquantity;
    private String orderDtopaymentMethod;
    private boolean orderDtoisAccept;

    private String orderDtopaymentId;
    private String orderDtostatus; 

    private boolean orderDtoisDelivered;
    
}
