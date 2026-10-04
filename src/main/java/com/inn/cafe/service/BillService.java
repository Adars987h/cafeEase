package com.inn.cafe.service;

import com.inn.cafe.POJO.Order;

public interface BillService {

    /** Renders the invoice for an order as PDF bytes. */
    byte[] generateBill(Order order);
}
