package com.inn.cafe.rest;


import com.inn.cafe.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping(path = "/category")
public interface CategoryRest {

    @PostMapping(path = "/add")
    ResponseEntity<Response> addNewCategory(@RequestBody(required = true) Map<String, String> requestMap);

    @GetMapping("/get")
    ResponseEntity<Response> getAllCategories(@RequestParam(required = false) String filterValue);

    @PostMapping("/update")
    ResponseEntity<Response> updateCategory(@RequestBody(required = true)Map<String,String> requestMap);
}
