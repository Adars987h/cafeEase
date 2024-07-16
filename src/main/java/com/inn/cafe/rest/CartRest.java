package com.inn.cafe.rest;

import com.inn.cafe.POJO.Cart;
import com.inn.cafe.dto.OrderItem;
import com.inn.cafe.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/cart")
public interface CartRest {

    @PutMapping
    ResponseEntity<Response> addOrUpdateItem(@RequestBody List<OrderItem> items);
}
