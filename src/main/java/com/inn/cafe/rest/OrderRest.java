package com.inn.cafe.rest;


import com.inn.cafe.POJO.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(path = "/orders")
public interface OrderRest {

    @PostMapping
    ResponseEntity<Order> placeOrder();

//    @GetMapping
//    ResponseEntity<Order> getOrders();

}
