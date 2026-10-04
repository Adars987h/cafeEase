package com.inn.cafe.restImpl;

import com.inn.cafe.POJO.Order;
import com.inn.cafe.dto.Response;
import com.inn.cafe.rest.BillRest;
import com.inn.cafe.service.BillService;
import com.inn.cafe.service.OrderService;
import com.inn.cafe.utils.EmailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
public class BillRestImpl implements BillRest {

    @Autowired
    BillService billService;

    @Autowired
    OrderService orderService;

    @Autowired
    EmailUtils emailUtils;

    @Override
    public ResponseEntity<byte[]> downloadBill(Integer orderId) {
        // Exact id lookup with an ownership check. It used to take the first hit of a
        // substring search, so a bill could be rendered for the wrong order.
        Order order = orderService.getVisibleOrder(orderId);
        byte[] pdf = billService.generateBill(order);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName(order)).build());
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }

    @Override
    public ResponseEntity<Response> emailBill(Integer orderId) {
        Order order = orderService.getVisibleOrder(orderId);
        String to = order.getCustomer().getEmail();
        try {
            emailUtils.sendWithAttachment(to, "Your CafeEase bill for order #" + order.getOrderId(),
                    "<p>Thanks for ordering with CafeEase. Your bill for order #" + order.getOrderId()
                            + " is attached.</p>",
                    fileName(order), billService.generateBill(order));
        } catch (Exception ex) {
            log.error("Bill email for order {} failed: {}", orderId, ex.getMessage());
            return new ResponseEntity<>(new Response("We couldn't send the email right now. Download the bill instead."),
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
        return new ResponseEntity<>(new Response("Bill sent to " + to + "."), HttpStatus.OK);
    }

    private static String fileName(Order order) {
        return "CafeEase-Invoice-" + order.getOrderId() + ".pdf";
    }
}
