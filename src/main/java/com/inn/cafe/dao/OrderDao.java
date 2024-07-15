package com.inn.cafe.dao;

import com.inn.cafe.POJO.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface OrderDao extends JpaRepository<Order, Integer> {

}