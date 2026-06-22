package com.example.zlagoda.model;

import lombok.Data;

@Data
public class Product {
    private Integer id_product;
    private Integer category_number;
    private String product_name;
    private String producer;
    private String characteristics;
}
