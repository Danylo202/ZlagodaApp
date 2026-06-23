package com.example.zlagoda.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sale {
    private String UPC;
    private Integer checkNumber;
    private Integer productNumber;
    private Double sellingPrice;

    private String productName; // joined data
}