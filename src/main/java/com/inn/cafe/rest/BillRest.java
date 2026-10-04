package com.inn.cafe.rest;


import com.inn.cafe.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/bill")
public interface BillRest {

    // PDF of one order's invoice, for the order's customer or an admin.
    @GetMapping("/{orderId}")
    ResponseEntity<byte[]> downloadBill(@PathVariable Integer orderId);

    // Emails the same PDF to the order's customer.
    @PostMapping("/{orderId}/email")
    ResponseEntity<Response> emailBill(@PathVariable Integer orderId);
}
