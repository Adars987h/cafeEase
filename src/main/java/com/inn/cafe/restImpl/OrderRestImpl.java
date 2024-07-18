package com.inn.cafe.restImpl;

import com.inn.cafe.POJO.Order;
import com.inn.cafe.POJO.User;
import com.inn.cafe.dto.OrderItem;
import com.inn.cafe.dto.OrderSearchRequest;
import com.inn.cafe.dto.Response;
import com.inn.cafe.rest.OrderRest;
import com.inn.cafe.service.OrderService;
import com.inn.cafe.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Slf4j
@RestController
public class OrderRestImpl implements OrderRest {

    @Autowired
    OrderService orderService;

    @Autowired
    UserService userService;

    @Override
    public ResponseEntity<Response> placeOrder() {
        try{
            Order order = orderService.placeOrder();
            return new ResponseEntity<>(new Response(order), HttpStatus.OK);
        }catch (Exception ex){
            log.error(ex.getMessage());
            ex.printStackTrace();
            return new ResponseEntity<>(new Response(ex.getMessage()),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Response> getOrders(OrderSearchRequest orderSearchRequest) {
        try{
            List<Order> orders = orderService.searchOrders(orderSearchRequest);
            Response response = new Response(orders);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (Exception ex){
            log.error(ex.getMessage());
            ex.printStackTrace();
            return new ResponseEntity<>(new Response(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public ResponseEntity<Response> cancelOrder(Integer orderId) {
        try{
            String result=orderService.cancelOrder(orderId);
            Response response = new Response(result);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch(Exception ex){
            log.error(ex.getMessage());

            return new ResponseEntity<>(new Response(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Response> orderByAdmin(String emailId, List<OrderItem> items) {
        try{
            User user= userService.findByEmail(emailId);
            
            Order order= orderService.orderByAdmin(user,items);
            Response response = new Response(order);
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        }catch (Exception ex){
            log.error(ex.getMessage());

            return  new ResponseEntity<>(new Response(ex.getMessage()),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
