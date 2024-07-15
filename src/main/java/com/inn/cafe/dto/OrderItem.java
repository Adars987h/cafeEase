package com.inn.cafe.dto;


import lombok.Data;

@Data
public class OrderItem {

    private int productId ;

    private String productName;

    private int quantity;

    private float pricePerUnit;

    private float price;

}