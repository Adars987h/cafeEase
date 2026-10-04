package com.inn.cafe.service;


import com.inn.cafe.POJO.Order;
import com.inn.cafe.POJO.User;
import com.inn.cafe.dto.OrderItem;
import com.inn.cafe.dto.OrderSearchRequest;
import com.inn.cafe.dto.TopSellerResponse;

import java.util.List;

public interface OrderService {
    Order placeOrder();

    List<Order> searchOrders(OrderSearchRequest orderSearchRequest);

    Order cancelOrder(Integer orderId);

    Order updateStatus(Integer orderId, String status);

    /** The order if it exists and the current user may see it; throws NotFoundException otherwise. */
    Order getVisibleOrder(Integer orderId);

    Order orderByAdmin(User user, List<OrderItem> items);

    // Aggregate-only, no customer data -- safe for the public landing page.
    TopSellerResponse getTopSellerToday();

}
