package com.inn.cafe.POJO;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inn.cafe.dto.OrderItem;
import com.inn.cafe.enums.OrderStatus;
import com.inn.cafe.wrapper.CustomerWrapper;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "`order`")
public class Order {

    private static final long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orderIdSequence")
    @SequenceGenerator(name = "orderIdSequence", initialValue = 65000001, allocationSize = 1)
    @Id
    @Column(name = "orderid")
    private int orderId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_fk", nullable = false)
    private User customer;

    @Transient
    private CustomerWrapper customerDetails;

    @Column(name = "totalamount")
    private float totalAmount;

    @JsonIgnore
    @Column(name = "items", columnDefinition = "json")
    private String itemsJson;

    @Transient
    private List<OrderItem> items;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderDateAndTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "orderstatus", columnDefinition = "nvarchar(500)")
    private OrderStatus orderStatus;

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
        this.items = objectMapper.readValue(this.itemsJson, new TypeReference<List<OrderItem>>() {});
        this.customerDetails = new CustomerWrapper(this.customer);
    }
}
