package com.inn.cafe.dao;

import com.inn.cafe.POJO.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryDao extends JpaRepository<Category,Integer> {


//    @Query("SELECT c FROM Category c WHERE c.name LIKE %:name%")
    List<Category> getAllCategory(@Param("name") String name);

    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.name = :name")
    boolean isPresent(@Param("name") String name);


}
