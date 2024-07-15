package com.inn.cafe.restImpl;

import com.inn.cafe.POJO.Order;
import com.inn.cafe.rest.OrderRest;
import com.inn.cafe.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
public class OrderRestImpl implements OrderRest {

    @Autowired
    OrderService orderService;

    @Override
    public ResponseEntity<Order> placeOrder() {
        try{
            Order order = orderService.placeOrder();
            return new ResponseEntity<>(order, HttpStatus.OK);
        }catch (Exception ex){
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
