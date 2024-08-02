package com.inn.cafe.rest;

import com.inn.cafe.dto.OrderItem;
import com.inn.cafe.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/cart")
public interface CartRest {

    @PutMapping
    ResponseEntity<Response> addOrUpdateItem(@RequestBody List<OrderItem> items);

    @GetMapping
    ResponseEntity<Response> getCart(@RequestParam(required = false) String emailId);
}
