package com.inn.cafe.service;


import com.inn.cafe.POJO.Order;
import com.inn.cafe.POJO.User;
import com.inn.cafe.dto.OrderItem;
import com.inn.cafe.dto.OrderSearchRequest;

import java.util.List;

public interface OrderService {
    Order placeOrder();

    List<Order> searchOrders(OrderSearchRequest orderSearchRequest);

    String cancelOrder(Integer orderId);

    Order orderByAdmin(User user, List<OrderItem> items);

}
