package com.example.zlagoda.model;

import lombok.Data;

@Data
public class Product {
    private Integer id_product;
    private Integer category_number;
    private String product_name;
    private String characteristics;
    private String producer;

    public Product(Integer id_product, Integer category_number, String product_name, String characteristics, String producer) {
        this.id_product = id_product;
        this.category_number = category_number;
        this.product_name = product_name;
        this.characteristics = characteristics;
        this.producer = producer;
    }

    public Integer getIdProduct() {
        return this.id_product;
    }
    public Integer getCategoryNumber() {
        return this.category_number;
    }
    public String getProductName() {
        return this.product_name;
    }
    public String getCharacteristics() {
        return this.characteristics;
    }
    public String getProducer() {
        return this.producer;
    }
}
