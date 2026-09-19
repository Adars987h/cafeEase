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


    // #customer itself is null for an admin's unscoped search (no customer filter
    // requested); the property accessors below used unguarded SpEL navigation
    // (:#{#customer.name}) which threw "Property or field 'name' cannot be found
    // on null" as soon as #customer was null, so every admin call to /orders/search
    // with no customer in the body failed outright. Guarded with ?. so a null
    // customer just means "no filter" instead of an error.
    @Query("SELECT o FROM Order o WHERE ((:#{#customer?.name} IS NULL OR o.customer.name like %:#{#customer?.name}%) AND (:#{#customer?.email} IS NULL OR o.customer.email = :#{#customer?.email}) AND (:#{#customer?.contactNumber} IS NULL OR o.customer.contactNumber = :#{#customer?.contactNumber}) AND (:#{#customer?.id} IS NULL OR o.customer.id = :#{#customer?.id})) AND o.orderDateAndTime BETWEEN :startTime AND :endTime order by o.orderDateAndTime desc")
    List<Order> findByUserAndTime(User customer, LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT o FROM Order o WHERE ((:#{#customer?.name} IS NULL OR o.customer.name like %:#{#customer?.name}%) AND (:#{#customer?.email} IS NULL OR o.customer.email = :#{#customer?.email}) AND (:#{#customer?.contactNumber} IS NULL OR o.customer.contactNumber = :#{#customer?.contactNumber}) AND (:#{#customer?.id} IS NULL OR o.customer.id = :#{#customer?.id})) AND o.orderDateAndTime BETWEEN :startTime AND :endTime AND (:orderId IS NULL OR STR(o.orderId) LIKE %:orderId%) order by o.orderDateAndTime desc")
    List<Order> findByUserTimeAndOrderId(User customer, LocalDateTime startTime, LocalDateTime endTime, Integer orderId);

    @Query("SELECT o FROM Order o WHERE o.orderDateAndTime BETWEEN :startTime AND :endTime AND (:orderId IS NULL OR STR(o.orderId) LIKE %:orderId%) order by o.orderDateAndTime desc")
    List<Order> findByTimeAndOrderId(LocalDateTime startTime, LocalDateTime endTime, Integer orderId);

    Order findByOrderId(Integer orderId);
}