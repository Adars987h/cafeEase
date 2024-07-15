package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.CustomerUserDetailsService;
import com.inn.cafe.POJO.Cart;
import com.inn.cafe.POJO.Order;
import com.inn.cafe.POJO.User;
import com.inn.cafe.dao.CartDao;
import com.inn.cafe.dao.OrderDao;
import com.inn.cafe.enums.OrderStatus;
import com.inn.cafe.service.CartService;
import com.inn.cafe.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderDao orderDao;

    @Autowired
    CartDao cartDao;

    @Autowired
    CartService cartService;

    @Autowired
    CustomerUserDetailsService customerUserDetailsService;

    @Override
    public Order placeOrder() {
        try {
            User user = customerUserDetailsService.getUserDetail();
            Cart cart = cartDao.getCartByCustomerId(user);

            Order order = new Order();
            order.setCustomerId(user);
            order.setItems(cart.getItems());
            order.setTotalAmount(cart.getTotalAmount());
            order.setOrderDateAndTime(LocalDateTime.now());
            order.setOrderStatus(OrderStatus.ORDER_PLACED);

            Order placedOrder = orderDao.save(order);
            cartService.emptyCart();
            return placedOrder;

        } catch (Exception ex) {
            throw ex;
        }

    }
}
