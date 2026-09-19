package com.inn.cafe.rest;


import com.inn.cafe.dto.OrderItem;
import com.inn.cafe.dto.OrderSearchRequest;
import com.inn.cafe.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(path = "/orders")
public interface OrderRest {

    // Public, aggregate-only (product name/price/quantity, no customer data) --
    // powers the "today's most ordered" claim on the public landing page.
    @GetMapping("/top-seller")
    ResponseEntity<Response> getTopSellerToday();

    @PostMapping
    ResponseEntity<Response> placeOrder();

    @PostMapping("/search")
    ResponseEntity<Response> getOrders(@RequestBody OrderSearchRequest orderSearchRequest);

    @PostMapping("/cancel/{id}")
    ResponseEntity<Response> cancelOrder(@PathVariable(name="id") Integer orderId);

    @PostMapping("/admin")
    ResponseEntity<Response> orderByAdmin(@RequestParam String emailId, @RequestBody List<OrderItem> items);

}
