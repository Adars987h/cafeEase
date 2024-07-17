package com.inn.cafe.rest;


import com.inn.cafe.dto.OrderSearchRequest;
import com.inn.cafe.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(path = "/orders")
public interface OrderRest {

    @PostMapping
    ResponseEntity<Response> placeOrder();

    @PostMapping("/search")
    ResponseEntity<Response> getOrders(@RequestBody OrderSearchRequest orderSearchRequest);

    @PostMapping("/cancel/{id}")
    ResponseEntity<Response> cancelOrder(@PathVariable(name="id") Integer orderId);

}
