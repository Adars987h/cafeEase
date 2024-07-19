package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Category;
import com.inn.cafe.POJO.Product;
import com.inn.cafe.constants.CafeConstants;
import com.inn.cafe.dao.ProductDao;
import com.inn.cafe.exceptions.BadRequestException;
import com.inn.cafe.exceptions.UnauthorizedException;
import com.inn.cafe.service.ProductService;
import com.inn.cafe.wrapper.ProductWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductDao productDao;

    @Autowired
    JwtFilter jwtFilter;

    @Override
    public ProductWrapper addNewProduct(Map<String, String> requestMap) {
        try{
            if(jwtFilter.isAdmin()){
                if(validateProductMap(requestMap,false)){
                    if (productDao.isPresent(requestMap.get("name"))) {
                        throw new BadRequestException("Product with name " + requestMap.get("name") + " already exists.");
                    }
                    Product product = productDao.save(getProductFromMap(requestMap, false));
                    return new ProductWrapper(product);
                }
                throw new BadRequestException(CafeConstants.INVALID_PAYLOAD);
            } else {
                throw new UnauthorizedException(CafeConstants.UNAUTHORISED_ACCESS);
            }
        }catch(Exception ex){
            log.error(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public List<ProductWrapper> getAllProduct() {
        try{
            if (jwtFilter.isAdmin())
                return productDao.getAllProduct();
            throw new UnauthorizedException("You don't have required permissions to perform this operation");
        }catch (Exception ex){
            log.error(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public ProductWrapper updateProduct(Map<String, String> requestMap) {
        try{
            if(jwtFilter.isAdmin()){
                if(validateProductMap(requestMap,true)){
                    Optional<Product> optional=productDao.findById(Integer.parseInt(requestMap.get("id")));
                    if(!optional.isEmpty()){
                        Product product=getProductFromMap(requestMap,true);
                        product.setStatus(optional.get().getStatus());
                        Product createdProduct = productDao.save(product);
                        return new ProductWrapper(createdProduct);
                    }
                    else{
                        throw new BadRequestException("Product id does not exist");
                    }
                }else {
                    throw new BadRequestException(CafeConstants.INVALID_PAYLOAD);
                }
            } else{
                throw new UnauthorizedException(CafeConstants.UNAUTHORISED_ACCESS);
            }

        }catch(Exception ex){
            log.error(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public String deleteProduct(Integer id) {
        try{
            if(jwtFilter.isAdmin()){
                Optional optional= productDao.findById(id);
                if(!optional.isEmpty()){
                    productDao.deleteById(id);
                    return "Product deleted successfully";
                }
                throw new BadRequestException("Product id does not exist");
            }else {
                throw new UnauthorizedException(CafeConstants.UNAUTHORISED_ACCESS);
            }
        }catch (Exception ex){
//            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public String updateStatus(Map<String, String> requestMap) {
        try{
            if(jwtFilter.isAdmin()) {
                Optional optional = productDao.findById(Integer.parseInt(requestMap.get("id")));
                if (!optional.isEmpty()) {
                    productDao.updateProductStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
                    return "Product Status Updated Successfully";
                } else {
                    throw new BadRequestException("Product id does not exist");
                }
            } else{
                throw new UnauthorizedException(CafeConstants.UNAUTHORISED_ACCESS);
            }

        }catch(Exception ex){
//            log.error(ex.getMessage());
//            ex.printStackTrace();
            throw ex;
        }

    }

    @Override
    public List<ProductWrapper> getByCategory(int id) {
        try{
            return productDao.getProductByCategory(id);
        }catch(Exception ex){
//            log.error(ex.getMessage());
//            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public ProductWrapper getProductById(int id) {
        try{
            return productDao.getProductById(id);
        }catch (Exception ex){
//            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public List<Product> getProductsBasedOnFilter(String categoryName, String productName, String status) {
        try{
            categoryName = !StringUtils.isEmpty(categoryName)? categoryName : "";
            productName = !StringUtils.isEmpty(productName)? productName : "";
            return productDao.getProductBasedOnFilter(categoryName, productName, status);
        }catch (Exception ex){
            log.error(ex.getMessage());
            throw ex;
        }
    }

    private boolean validateProductMap(Map<String, String> requestMap, boolean validateId) {
        if(requestMap.containsKey("name") ){
            if(requestMap.containsKey("id") && validateId){
                return true;
            } else if (!validateId) {
                return true;
            }
        }
        return false;
    }

    private Product getProductFromMap(Map<String, String> requestMap, boolean isAdd) {
        Category category=new Category();
        category.setId(Integer.parseInt(requestMap.get("categoryId")));


        Product product=new Product();
        if(isAdd){
            product.setId(Integer.parseInt(requestMap.get("id")));
        }else{
            product.setStatus("true");
        }
        product.setCategory(category);
        product.setName(requestMap.get("name"));
        product.setDescription(requestMap.get("description"));
        product.setPrice(Float.parseFloat(requestMap.get("price")));

        return product;
    }



}
