package com.inn.cafe.rest;


import com.inn.cafe.dto.OrderSearchRequest;
import com.inn.cafe.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(path = "/orders")
public interface OrderRest {

    @PostMapping
    ResponseEntity<Response> placeOrder();

    @PostMapping("/search")
    ResponseEntity<Response> getOrders(@RequestBody OrderSearchRequest orderSearchRequest);

}
