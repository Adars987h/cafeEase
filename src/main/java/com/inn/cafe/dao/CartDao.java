package com.inn.cafe.dao;

import com.inn.cafe.POJO.Cart;
import com.inn.cafe.POJO.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartDao extends JpaRepository<Cart, Integer> {

    Cart findByCustomerId(User user);

    Cart getCartByCustomerId(User user);
}
