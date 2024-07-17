package com.inn.cafe.service;


import com.inn.cafe.POJO.Order;
import com.inn.cafe.dto.OrderSearchRequest;

import java.util.List;

public interface OrderService {
    Order placeOrder();

    List<Order> searchOrders(OrderSearchRequest orderSearchRequest);

    String cancelOrder(Integer orderId);

}
