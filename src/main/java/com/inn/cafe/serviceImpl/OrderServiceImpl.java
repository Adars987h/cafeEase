package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.CustomerUserDetailsService;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Cart;
import com.inn.cafe.POJO.Order;
import com.inn.cafe.POJO.User;
import com.inn.cafe.dao.CartDao;
import com.inn.cafe.dao.OrderDao;
import com.inn.cafe.dto.OrderSearchRequest;
import com.inn.cafe.enums.OrderStatus;
import com.inn.cafe.service.CartService;
import com.inn.cafe.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


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

    @Autowired
    JwtFilter jwtFilter;

    @Override
    public Order placeOrder() {
        try {
            User user = customerUserDetailsService.getUserDetail();
            Cart cart = cartDao.getCartByCustomerId(user);
            if (cart.getItems().isEmpty()){
                throw new RuntimeException("Cart Is Empty, Please add items to cart for placing order");
            }
            Order order = new Order();
            order.setCustomer(user);
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

    @Override
    public List<Order> searchOrders(OrderSearchRequest orderSearchRequest) {
        try {
            boolean isUser = jwtFilter.isUser();
            User currentUser = customerUserDetailsService.getUserDetail();
            User customerFromRequest = orderSearchRequest.getCustomer();
            if (isUser){
                if (Objects.nonNull(customerFromRequest)){
                    validateOrderSearchRequestCustomer(customerFromRequest, currentUser);
                }

                orderSearchRequest.setCustomer(currentUser);
            }
            List<Order> orders = new ArrayList<>();
            if (Objects.isNull(orderSearchRequest.getOrderId())){
                orders = orderDao.findByUserAndTime(customerFromRequest, orderSearchRequest.getStartTime(), orderSearchRequest.getEndTime());
            }else {
                if (Objects.nonNull(customerFromRequest) && Objects.nonNull(orderSearchRequest.getOrderId())){
                    orders = orderDao.findByUserTimeAndOrderId(customerFromRequest, orderSearchRequest.getStartTime(), orderSearchRequest.getEndTime(), orderSearchRequest.getOrderId());
                } else {
                    if (!isUser){
                        orders = orderDao.findByTimeAndOrderId(orderSearchRequest.getStartTime(), orderSearchRequest.getEndTime(), orderSearchRequest.getOrderId());
                    }
                }
            }
            return orders;
        }catch (Exception ex){
            throw ex;
        }
    }

    private static void validateOrderSearchRequestCustomer(User customerFromRequest, User currentUser) {
        if (Objects.nonNull(customerFromRequest.getEmail()) && !(customerFromRequest.getEmail().equalsIgnoreCase(currentUser.getEmail())))
            throw new RuntimeException("Insufficient Privilege: User cannot search for other users");
        if (Objects.nonNull(customerFromRequest.getId()) && (!customerFromRequest.getId().equals(currentUser.getId())))
            throw new RuntimeException("Insufficient Privilege: User cannot search for other users");
        if (Objects.nonNull(customerFromRequest.getName()) && !(customerFromRequest.getName().equalsIgnoreCase(currentUser.getName())))
            throw new RuntimeException("Insufficient Privilege: User cannot search for other users");
        if (Objects.nonNull(customerFromRequest.getContactNumber()) && !(customerFromRequest.getContactNumber().equalsIgnoreCase(currentUser.getContactNumber())))
            throw new RuntimeException("Insufficient Privilege: User cannot search for other users");
    }
}
