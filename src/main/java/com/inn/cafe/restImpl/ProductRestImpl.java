package com.inn.cafe.restImpl;

import com.inn.cafe.constants.CafeConstants;
import com.inn.cafe.dto.Response;
import com.inn.cafe.rest.ProductRest;
import com.inn.cafe.service.ProductService;
import com.inn.cafe.utils.CafeUtils;
import com.inn.cafe.wrapper.ProductWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
@Slf4j
public class ProductRestImpl implements ProductRest {

   @Autowired
    ProductService productService;

    @Override
    public ResponseEntity<Response> addNewProduct(Map<String, String> requestMap) {
        try{
            ProductWrapper productWrapper = productService.addNewProduct(requestMap);
            return new ResponseEntity<>(new Response(productWrapper, "Product Added Successfully"), HttpStatus.OK);
        }catch (Exception ex){
            log.error(ex.getMessage());
            ex.printStackTrace();
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
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public ResponseEntity<Response> updateProduct(Map<String, String> requestMap) {
        try{
            ProductWrapper productWrapper = productService.updateProduct(requestMap);
            return new ResponseEntity<>(new Response(productWrapper, "Successfully Updated Product"), HttpStatus.OK);
        }catch(Exception ex){
            log.error(ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public ResponseEntity<Response> deleteProduct(Integer id) {
        try{
            String result = productService.deleteProduct(id);
            return new ResponseEntity<>(new Response(result), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
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
            ex.printStackTrace();
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
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public ResponseEntity<Response> getProductById(int id) {
        try{
            ProductWrapper product = productService.getProductById(id);
            return new ResponseEntity<>(new Response(product), HttpStatus.OK);
        }catch(Exception ex){
            log.error(ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }


}
