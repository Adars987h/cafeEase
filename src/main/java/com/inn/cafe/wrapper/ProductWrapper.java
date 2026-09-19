package com.inn.cafe.wrapper;


import com.inn.cafe.POJO.Product;
import lombok.Data;

import java.util.Base64;

@Data
public class ProductWrapper {

    Integer id;

    String name;

    String description;

    Float price;

    String status;

    Integer categoryId;

    String categoryName;

    // Base64, matching the convention CategoryDTO already uses for its image
    // field -- the FE renders both the same way (data:image/jpeg;base64,...).
    String image;

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

    public ProductWrapper(int id,String name,String description, float price, String status, int categoryId, String categoryName, byte[] image){
        this(id, name, description, price, status, categoryId, categoryName);
        this.image = encode(image);
    }

    public ProductWrapper(Product product){
        this.id=product.getId();
        this.name= product.getName();
        this.description= product.getDescription();
        this.price=product.getPrice();
        this.status=product.getStatus();
        this.categoryId=product.getCategory().getId();
        this.categoryName=product.getCategory().getName();
        this.image=encode(product.getImage());
    }

    public ProductWrapper(int id, String name, String description,float price){
        this.id=id;
        this.name=name;
        this.description=description;
        this.price=price;
    }

    private static String encode(byte[] image) {
        return image == null ? null : Base64.getEncoder().encodeToString(image);
    }

}
