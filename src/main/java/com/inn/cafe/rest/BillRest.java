package com.inn.cafe.rest;


import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@RequestMapping("/bill")
public interface BillRest {

//    @GetMapping("/generateBill/{id}")
//    ResponseEntity<String> generateBill(@PathVariable Integer id);
//
//    @GetMapping("/downloadPdf/{id}")
//    public ResponseEntity<InputStreamResource> downloadPdf(@PathVariable Integer id);

    @GetMapping("/{orderId}")
    ResponseEntity<InputStreamResource> downloadBill(@PathVariable Integer orderId) throws IOException;

}
