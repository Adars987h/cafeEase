package com.inn.cafe.service;

import com.inn.cafe.POJO.Cart;
import com.inn.cafe.dto.OrderItem;

import java.util.List;

public interface CartService {

    Cart addOrUpdateItem(List<OrderItem> items);

    void emptyCart();


}
