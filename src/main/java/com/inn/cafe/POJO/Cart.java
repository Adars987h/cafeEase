package com.inn.cafe.POJO;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inn.cafe.dto.OrderItem;
import com.inn.cafe.wrapper.CustomerWrapper;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.IOException;
import java.util.List;

@Data
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "cart")
public class Cart {

    private static final long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_fk", nullable = false)
    private User customerId;

    @Transient
    private CustomerWrapper customerDetails;

    @Column(name = "totalamount")
    private float totalAmount;

    @JsonIgnore
    @Column(name = "items", columnDefinition = "json")
    private String itemsJson;

    @Transient
    private List<OrderItem> items;

    @PrePersist
    @PreUpdate
    public void prePersist() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        this.itemsJson = objectMapper.writeValueAsString(this.items);
    }

    @PostLoad
    @PostPersist
    @PostUpdate
    public void postLoad() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        this.items = objectMapper.readValue(this.itemsJson, new TypeReference<List<OrderItem>>() {
        });
        this.customerDetails = new CustomerWrapper(this.customerId);
    }
}
