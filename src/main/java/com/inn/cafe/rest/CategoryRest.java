package com.inn.cafe.rest;


import com.inn.cafe.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping(path = "/category")
public interface CategoryRest {

    @PostMapping
    ResponseEntity<Response> addNewCategory(@RequestParam String name, @RequestParam(value = "image", required = false) MultipartFile image);

    @GetMapping
    ResponseEntity<Response> getAllCategories(@RequestParam(required = false) String filterValue);

    @PutMapping("/{id}")
    ResponseEntity<Response> updateCategory(@PathVariable int id, @RequestParam String name, @RequestParam(required = false) MultipartFile image);

}
