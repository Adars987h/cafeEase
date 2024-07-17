package com.inn.cafe.rest;


import com.inn.cafe.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/product")
public interface ProductRest {

    @PostMapping("/add")
    ResponseEntity<Response> addNewProduct(@RequestBody Map<String,String> requestMap);

    @GetMapping("/get")
    ResponseEntity<Response> getAllProduct();

    @PostMapping("/update")
    ResponseEntity<Response> updateProduct(@RequestBody Map<String,String> requestMap);

    @PostMapping("/delete/{id}")
    ResponseEntity<Response> deleteProduct(@PathVariable Integer id);

    @PostMapping("/updateStatus")
    ResponseEntity<Response> updateStatus(@RequestBody Map<String,String> requestMap);

    @GetMapping("/getByCategory/{id}")
    ResponseEntity<Response> getByCategory(@PathVariable Integer id);

    @GetMapping("/getById/{id}")
    ResponseEntity<Response> getProductById(@PathVariable Integer id);

}
