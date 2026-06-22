package com.example.zlagoda.model;

import lombok.*;

@Data
@NoArgsConstructor 
@AllArgsConstructor
public class StoreProduct {
    private Integer UPC;
    private Integer UPC_prom;
    private Integer idProduct;
    private Double sellingPrice;
    private Integer productsNumber;
}
