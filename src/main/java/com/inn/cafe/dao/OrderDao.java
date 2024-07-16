package com.inn.cafe.dao;

import com.inn.cafe.POJO.Order;
import com.inn.cafe.POJO.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface OrderDao extends JpaRepository<Order, Integer> {


    @Query("SELECT o FROM Order o WHERE ((:#{#customer.name} IS NULL OR o.customer.name like %:#{#customer.name}%) AND (:#{#customer.email} IS NULL OR o.customer.email = :#{#customer.email}) AND (:#{#customer.contactNumber} IS NULL OR o.customer.contactNumber = :#{#customer.contactNumber}) AND (:#{#customer.id} IS NULL OR o.customer.id = :#{#customer.id})) AND o.orderDateAndTime BETWEEN :startTime AND :endTime")
    List<Order> findByUserAndTime(User customer, LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT o FROM Order o WHERE ((:#{#customer.name} IS NULL OR o.customer.name like %:#{#customer.name}%) AND (:#{#customer.email} IS NULL OR o.customer.email = :#{#customer.email}) AND (:#{#customer.contactNumber} IS NULL OR o.customer.contactNumber = :#{#customer.contactNumber}) AND (:#{#customer.id} IS NULL OR o.customer.id = :#{#customer.id})) AND o.orderDateAndTime BETWEEN :startTime AND :endTime AND o.orderId = :orderId")
    List<Order> findByUserTimeAndOrderId(User customer, LocalDateTime startTime, LocalDateTime endTime, Integer orderId);

    @Query("SELECT o FROM Order o WHERE o.orderDateAndTime BETWEEN :startTime AND :endTime AND o.orderId = :orderId")
    List<Order> findByTimeAndOrderId(LocalDateTime startTime, LocalDateTime endTime, Integer orderId);
}