package com.inn.cafe.serviceImpl;

import com.inn.cafe.POJO.Order;
import com.inn.cafe.service.BillService;
import com.inn.cafe.utils.BillCreatorUtil;
import com.inn.cafe.wrapper.CustomerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Builds invoices in memory. Previously each bill was written to /tmp, streamed, and
 * deleted 30s later by a raw thread - and the shared builder meant concurrent downloads
 * corrupted each other.
 */
@Slf4j
@Service
public class BillServiceImpl implements BillService {

    @Override
    public byte[] generateBill(Order order) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BillCreatorUtil bill = new BillCreatorUtil(out);
        try {
            bill.createDocument();
            bill.createCompanyHeader();
            bill.createInvoiceHeader(order);
            // The order's own customer - not whoever is downloading it (an admin's
            // download used to print the admin's details on the customer's bill).
            bill.createCustomerInformation(new CustomerWrapper(order.getCustomer()));
            bill.createTableHeader();
            bill.createItemsTable(order);
            bill.createTnc(); // closes the document
        } catch (IOException ex) {
            throw new UncheckedIOException("Bill generation failed for order " + order.getOrderId(), ex);
        }
        return out.toByteArray();
    }
}
