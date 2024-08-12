package com.inn.cafe.restImpl;

import com.inn.cafe.POJO.Category;
import com.inn.cafe.dto.CategoryDTO;
import com.inn.cafe.dto.Response;
import com.inn.cafe.rest.CategoryRest;
import com.inn.cafe.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class CategoryRestImpl implements CategoryRest {

    @Autowired
    CategoryService categoryService;

    @Override
    public ResponseEntity<Response> addNewCategory(String name, MultipartFile image) {
        try {
            Category category = categoryService.addNewCategory(name, image);
            return new ResponseEntity<>(new Response(category, "Category Added Successfully"), HttpStatus.OK);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public ResponseEntity<Response> getAllCategories(String filterValue) {
        try {
            List<Category> categories = categoryService.getAllCategories(filterValue);
            List<CategoryDTO> categoryDTOS = categories.stream().map(this::convertToDTO).collect(Collectors.toList());
            return new ResponseEntity<>(new Response(categoryDTOS), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public ResponseEntity<Response> updateCategory(int id, String name, MultipartFile image) {
        try {
            Category category = categoryService.updateCategory(id, name, image);
            return new ResponseEntity<>(new Response(category, "Category Updated Successfully"), HttpStatus.OK);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());

        if (category.getImage() != null) {
            String base64Image = Base64.getEncoder().encodeToString(category.getImage());
            dto.setImage(base64Image);
        }

        return dto;
    }
}
