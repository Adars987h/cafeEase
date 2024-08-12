package com.inn.cafe.service;

import com.inn.cafe.POJO.Category;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CategoryService {

    Category addNewCategory(String name, MultipartFile image);

    List<Category> getAllCategories(String filterValue);

    Category updateCategory(int id, String name, MultipartFile image);
}
