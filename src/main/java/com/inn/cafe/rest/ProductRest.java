package com.inn.cafe.rest;


import com.inn.cafe.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RequestMapping("/product")
public interface ProductRest {

    // multipart/form-data, not a JSON body, so a dish photo can ride along in
    // the same request -- matches the convention CategoryRest already uses.
    @PostMapping("/add")
    ResponseEntity<Response> addNewProduct(@RequestParam String name,
                                            @RequestParam String categoryId,
                                            @RequestParam String description,
                                            @RequestParam String price,
                                            @RequestParam(value = "image", required = false) MultipartFile image);

    @GetMapping("/admin")
    ResponseEntity<Response> getAllProduct();

    @PostMapping("/update")
    ResponseEntity<Response> updateProduct(@RequestParam String id,
                                            @RequestParam String name,
                                            @RequestParam String categoryId,
                                            @RequestParam String description,
                                            @RequestParam String price,
                                            @RequestParam(value = "image", required = false) MultipartFile image);

    @PostMapping("/delete/{id}")
    ResponseEntity<Response> deleteProduct(@PathVariable Integer id);

    @PostMapping("/updateStatus")
    ResponseEntity<Response> updateStatus(@RequestBody Map<String,String> requestMap);

    @GetMapping("/getByCategory/{id}")
    ResponseEntity<Response> getByCategory(@PathVariable Integer id);

    @GetMapping("/getById/{id}")
    ResponseEntity<Response> getProductById(@PathVariable Integer id);

    @GetMapping
    ResponseEntity<Response> getProducts(@RequestParam(required = false) String search);

    @PostMapping("/bulk")
    ResponseEntity<Response> addProductsAndCategoriesFromSql() throws Exception;

}
