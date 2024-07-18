package com.inn.cafe.service;

import com.inn.cafe.POJO.Order;
import com.inn.cafe.dto.BillResponse;
import com.inn.cafe.wrapper.CustomerWrapper;

import java.io.IOException;

public interface BillService {
    Boolean generateBill(CustomerWrapper customer, Order order);

    BillResponse getBillStream(Integer orderId) throws IOException;

    void deleteBill(Integer orderId) throws IOException;
}
