package com.inn.cafe.service;

import com.inn.cafe.POJO.Category;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface CategoryService {

    Category addNewCategory(Map<String,String> requestMap);

    List<Category> getAllCategories(String filterValue);

    Category updateCategory(Map<String,String> requestMap);
}
