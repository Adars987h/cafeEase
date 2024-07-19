package com.inn.cafe.dao;

import com.inn.cafe.POJO.Product;
import com.inn.cafe.wrapper.ProductWrapper;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;



public interface ProductDao extends JpaRepository<Product,Integer> {

    List<ProductWrapper> getAllProduct();


    @Modifying
    @Transactional
    Integer updateProductStatus(@Param("status") String status, @Param("id") int id);


    List<ProductWrapper> getProductByCategory(@Param("id") Integer id);

    ProductWrapper getProductById(@Param("id") int id);

    List<ProductWrapper> getProductsWithProductIdIn(List<Integer> productIds);

    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.name = :name")
    boolean isPresent(@Param("name") String name);

    @Query("SELECT p FROM Product p WHERE " +
            "(" +
            "(COALESCE(:categoryName, '') = '' OR p.category.name LIKE %:categoryName%) OR " +
            "(COALESCE(:productName, '') = '' OR p.name LIKE %:productName%)" +
            ") AND " +
            "p.status = :status")
    List<Product> getProductBasedOnFilter(@Param("categoryName") String categoryName,
                                          @Param("productName") String productName,
                                          @Param("status") String status);

}
