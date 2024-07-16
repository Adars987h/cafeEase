package com.inn.cafe.restImpl;

import com.inn.cafe.POJO.Cart;
import com.inn.cafe.dto.OrderItem;
import com.inn.cafe.dto.Response;
import com.inn.cafe.rest.CartRest;
import com.inn.cafe.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class CartRestImpl implements CartRest {

    @Autowired
    CartService cartService;

    @Override
    public ResponseEntity<Response> addOrUpdateItem(@RequestBody List<OrderItem> items) {
        try {
            Cart cart = cartService.addOrUpdateItem(items);
            return new ResponseEntity<>(new Response(cart), HttpStatus.OK);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
            return new ResponseEntity<>(new Response(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
