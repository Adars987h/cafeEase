package com.inn.cafe.rest;


import com.inn.cafe.POJO.Category;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/category")
public interface CategoryRest {

    @PostMapping(path = "/add")
    ResponseEntity<String> addNewCategory(@RequestBody(required = true) Map<String, String> requestMap);

    @GetMapping("/get")
    ResponseEntity<List<Category>> getAllCategories(@RequestParam(required = false) String filterValue);

    @PostMapping("/update")
    ResponseEntity<String> updateCategory(@RequestBody(required = true)Map<String,String> requestMap);
}
