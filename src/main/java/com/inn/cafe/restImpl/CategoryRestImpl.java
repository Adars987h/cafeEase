package com.inn.cafe.restImpl;

import com.inn.cafe.POJO.Category;
import com.inn.cafe.dto.Response;
import com.inn.cafe.rest.CategoryRest;
import com.inn.cafe.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class CategoryRestImpl implements CategoryRest {

    @Autowired
    CategoryService categoryService;

    @Override
    public ResponseEntity<Response> addNewCategory(Map<String, String> requestMap) {
        try{
            Category category = categoryService.addNewCategory(requestMap);
            return new ResponseEntity<>(new Response(category, "Category Added Successfully"), HttpStatus.OK);
        }catch (Exception ex){
            log.error(ex.getMessage());
//            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public ResponseEntity<Response> getAllCategories(String filterValue) {
        try{
            List<Category> categories = categoryService.getAllCategories(filterValue);
            return new ResponseEntity<>(new Response(categories), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public ResponseEntity<Response> updateCategory(Map<String, String> requestMap) {
        try{
            Category category = categoryService.updateCategory(requestMap);
            return new ResponseEntity<>(new Response(category, "Category Updated Successfully"), HttpStatus.OK);
        }catch(Exception ex){
            log.error(ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }
}
