package com.example.zlagoda.model;

import lombok.*;

@Data
@NoArgsConstructor 
@AllArgsConstructor
public class Product {
    private Integer idProduct;
    private Integer categoryNumber;
    private String productName;
    private String characteristics;
    private String producer;

    private String categoryName; // joined data
}
