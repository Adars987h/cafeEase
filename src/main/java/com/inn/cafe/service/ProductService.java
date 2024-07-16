package com.inn.cafe.service;

import com.inn.cafe.POJO.Product;
import com.inn.cafe.wrapper.ProductWrapper;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface ProductService {

    ProductWrapper addNewProduct(Map<String,String> requestMap);

    List<ProductWrapper> getAllProduct();

    ProductWrapper updateProduct(Map<String,String> requestMap);

    String deleteProduct(Integer id);

    String updateStatus(Map<String, String> requestMap);

    List<ProductWrapper> getByCategory(int id);

    ProductWrapper getProductById(int id);
}
