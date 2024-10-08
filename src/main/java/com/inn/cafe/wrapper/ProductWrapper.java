package com.inn.cafe.wrapper;


import com.inn.cafe.POJO.Product;
import lombok.Data;

@Data
public class ProductWrapper {

    Integer id;

    String name;

    String description;

    Float price;

    String status;

    Integer categoryId;

    String categoryName;
    public ProductWrapper(){

    }

    public ProductWrapper(int id,String name,String description, float price, String status, int categoryId, String categoryName){
        this.id=id;
        this.name=name;
        this.description=description;
        this.price=price;
        this.status=status;
        this.categoryId=categoryId;
        this.categoryName=categoryName;
    }

    public ProductWrapper(Product product){
        this.id=product.getId();
        this.name= product.getName();
        this.description= product.getDescription();
        this.price=product.getPrice();
        this.status=product.getStatus();
        this.categoryId=product.getCategory().getId();
        this.categoryName=product.getCategory().getName();
    }

    public ProductWrapper(int id, String name, String description,float price){
        this.id=id;
        this.name=name;
        this.description=description;
        this.price=price;
    }

}
