package com.inn.cafe.POJO;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inn.cafe.constants.BillConstants;
import com.inn.cafe.dto.OrderItem;
import com.inn.cafe.enums.OrderStatus;
import com.inn.cafe.wrapper.CustomerWrapper;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Column(name = "totalquantity")
    private int totalQuantity;

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

    public String toString(){
        StringBuilder productInformation = new StringBuilder();
        productInformation.append("\tS No. \t Description \t Price Per Unit \t Quantity \t Price\n");
        for (int i =1; i <= this.items.size(); i++){
            OrderItem item = this.items.get(i-1);
            productInformation.append("\t"+i+" \t\t "+item.getProductName() + " \t " + item.getPricePerUnit() + " \t\t\t\t " + item.getQuantity() + " \t\t\t " + item.getPrice() +"\n");
        }

        String order = "Your order Details : \n\n" +
                "Order Id \t- " + this.getOrderId()+"\n" +
                "Order Date \t-" + this.getOrderDateAndTime().toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) +"\n\n" +
                "Customer Information : \n" +
                "\t"+ BillConstants.CUSTOMER_ID +"\t\t\t-\t"+ this.getCustomer().getId() +"\n"+
                "\t"+ BillConstants.CUSTOMER_NAME +"\t\t-\t"+ this.getCustomer().getName() +"\n"+
                "\t"+ BillConstants.CUSTOMER_EMAIL +"\t\t-\t"+ this.getCustomer().getEmail() +"\n"+
                "\t"+ BillConstants.CUSTOMER_CONTACT_NO +"\t-\t"+ this.getCustomer().getContactNumber() +"\n"+
                "\n"+
                "Product Details : \n" + productInformation.toString() +
                "\tTotal : \t \t\t\t\t\t\t\t\t "+this.getTotalQuantity() +" \t\t\t "+ this.getTotalAmount();

        return order;
    }
}
