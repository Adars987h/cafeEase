package com.inn.cafe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Aggregate only -- product name/price/quantity sold. No customer or order
// data, so this is safe to expose on an unauthenticated endpoint for the
// public landing page ("today's most ordered").
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopSellerResponse {
    private Integer productId;
    private String productName;
    private float price;
    private int quantitySold;
}
