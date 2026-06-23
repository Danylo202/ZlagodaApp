package com.example.zlagoda.model;

import lombok.*;

@Data
@NoArgsConstructor 
public class StoreProduct {
    private String UPC;
    private String UPCProm;
    private Integer idProduct;
    private Double sellingPrice;
    private Integer productsNumber;
    private boolean promotionalProduct;

    private String productName; // joined data
    private String characteristics; // joined data
    private String producer; // joined data

    public StoreProduct(String UPC, String UPCProm, Integer idProduct, Double sellingPrice, Integer productsNumber, boolean promotionalProduct) {
        if((UPCProm==null && promotionalProduct==false) || (UPCProm!=null && promotionalProduct==true)) {
            this.UPC = UPC;
            this.UPCProm = UPCProm;
            this.idProduct = idProduct;
            this.sellingPrice = sellingPrice;
            this.productsNumber = productsNumber;
            this.promotionalProduct = promotionalProduct;
        }
        else {
            throw new IllegalArgumentException("Логічна помилка: акційний статус не збігається з наявністю UPCProm!");
        }
    }

    public boolean isValid() {
        return (UPCProm==null && promotionalProduct==false) || (UPCProm!=null && promotionalProduct==true);
    }
}
