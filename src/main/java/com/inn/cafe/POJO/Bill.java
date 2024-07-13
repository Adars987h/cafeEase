package com.inn.cafe.POJO;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;


@Data
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name="bill")
public class Bill implements Serializable {

    private static final long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name="id")
    private int id;

    @Column(name="uuid")
    private String uuid;

    @Column(name="name")
    private String name;

    @Column(name="email")
    private String email;

    @Column(name="contactnumber")
    private String contactNumber;

    @Column(name="paymentmethod")
    private String paymentMethod;

    @Column(name = "total")
    private int total;

    @Column(name="productdetails", columnDefinition = "json")
    private String productDetail;

    @Column(name="createdby")
    private String createdBy;






}
