package com.inn.cafe.restImpl;

import com.inn.cafe.JWT.CustomerUserDetailsService;
import com.inn.cafe.POJO.Cart;
import com.inn.cafe.POJO.User;
import com.inn.cafe.dto.OrderItem;
import com.inn.cafe.dto.Response;
import com.inn.cafe.exceptions.BadRequestException;
import com.inn.cafe.rest.CartRest;
import com.inn.cafe.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class CartRestImpl implements CartRest {

    @Autowired
    CartService cartService;

    @Autowired
    CustomerUserDetailsService customerUserDetailsService;


    @Override
    public ResponseEntity<Response> addOrUpdateItem(@RequestBody List<OrderItem> items) {
        try {
            Cart cart = cartService.addOrUpdateItem(items);
            return new ResponseEntity<>(new Response(cart), HttpStatus.OK);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }

    }

    @Override
    public ResponseEntity<Response> getCart(String emailId) {
        try {
            User userDetail = customerUserDetailsService.getUserDetail();
            Cart cart;
            if ("admin".equalsIgnoreCase(userDetail.getRole())){
                if (StringUtils.isEmpty(emailId)){
                    throw new BadRequestException("Email Id is required for searching cart of user");
                }
                cart =  cartService.getCartByUserEmail(emailId);
            }else {
                cart = cartService.getCartForUser(userDetail);
            }
            return new ResponseEntity<>(new Response(cart), HttpStatus.OK);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }
}
