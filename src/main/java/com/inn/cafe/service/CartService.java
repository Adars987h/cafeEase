package com.inn.cafe.service;

import com.inn.cafe.POJO.Cart;
import com.inn.cafe.POJO.User;
import com.inn.cafe.dto.OrderItem;

import java.util.List;

public interface CartService {

    Cart addOrUpdateItem(List<OrderItem> items);

    void emptyCart();

    Cart getCartByUserEmail(String emailId);

    Cart getCartForUser(User userDetail);
}
