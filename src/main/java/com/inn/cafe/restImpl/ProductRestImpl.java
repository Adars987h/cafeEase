package com.inn.cafe.restImpl;

import com.inn.cafe.POJO.Product;
import com.inn.cafe.dto.Response;
import com.inn.cafe.rest.ProductRest;
import com.inn.cafe.service.ProductService;
import com.inn.cafe.service.SqlExecService;
import com.inn.cafe.wrapper.ProductWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@Slf4j
public class ProductRestImpl implements ProductRest {

   @Autowired
    ProductService productService;

   @Autowired
    SqlExecService sqlExecService;

    @Override
    public ResponseEntity<Response> addNewProduct(String name, String categoryId, String description, String price, MultipartFile image) {
        try{
            Map<String, String> requestMap = new HashMap<>();
            requestMap.put("name", name);
            requestMap.put("categoryId", categoryId);
            requestMap.put("description", description);
            requestMap.put("price", price);
            ProductWrapper productWrapper = productService.addNewProduct(requestMap, image);
            return new ResponseEntity<>(new Response(productWrapper, "Product Added Successfully"), HttpStatus.OK);
        }catch (Exception ex){
            log.error(ex.getMessage());
//            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public ResponseEntity<Response> getAllProduct() {
        try{
            List<ProductWrapper> allProducts = productService.getAllProduct();
            return new ResponseEntity<>(new Response(allProducts), HttpStatus.OK);
        }catch(Exception ex){
            log.error(ex.getMessage());
//            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public ResponseEntity<Response> updateProduct(String id, String name, String categoryId, String description, String price, MultipartFile image) {
        try{
            Map<String, String> requestMap = new HashMap<>();
            requestMap.put("id", id);
            requestMap.put("name", name);
            requestMap.put("categoryId", categoryId);
            requestMap.put("description", description);
            requestMap.put("price", price);
            ProductWrapper productWrapper = productService.updateProduct(requestMap, image);
            return new ResponseEntity<>(new Response(productWrapper, "Successfully Updated Product"), HttpStatus.OK);
        }catch(Exception ex){
            log.error(ex.getMessage());
//            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public ResponseEntity<Response> deleteProduct(Integer id) {
        try{
            String result = productService.deleteProduct(id);
            return new ResponseEntity<>(new Response(result), HttpStatus.OK);
        }catch (Exception ex){
            log.error(ex.getMessage());
//            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public ResponseEntity<Response> updateStatus(Map<String, String> requestMap) {
        try{
            String result = productService.updateStatus(requestMap);
            return new ResponseEntity<>(new Response(result), HttpStatus.OK);
        }catch(Exception ex){
            log.error(ex.getMessage());
//            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public ResponseEntity<Response> getByCategory(Integer id) {
        try{
            List<ProductWrapper> products = productService.getByCategory(id);
            return new ResponseEntity<>(new Response(products), HttpStatus.OK);
        }catch(Exception ex){
            log.error(ex.getMessage());
//            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public ResponseEntity<Response> getProductById(Integer id) {
        try{
            ProductWrapper product = productService.getProductById(id);
            return new ResponseEntity<>(new Response(product), HttpStatus.OK);
        }catch(Exception ex){
            log.error(ex.getMessage());
//            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public ResponseEntity<Response> getProducts(String search) {
        try {
            List<Product> products = productService.getProductsBasedOnFilter(search, search, "true");
            List<ProductWrapper> productWrappers = products.stream().map(ProductWrapper::new).toList();
            return new ResponseEntity<>(new Response(productWrappers), HttpStatus.OK);
        }catch (Exception ex){
            log.error(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public ResponseEntity<Response> addProductsAndCategoriesFromSql() throws Exception {
        try {
            String file = "categories.sql";
            sqlExecService.executeSqlFromFile(file);
            file = "product.sql";
            sqlExecService.executeSqlFromFile(file);
            return new ResponseEntity<>(new Response("Successfully Added products"), HttpStatus.OK);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw ex;
        }
    }


}
