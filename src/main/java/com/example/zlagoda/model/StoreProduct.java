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
    private boolean promotional;

    public StoreProduct(String UPC, String UPCProm, Integer idProduct, Double sellingPrice, Integer productsNumber, boolean promotional) {
        if((UPCProm==null && promotional==false) || (UPCProm!=null && promotional==true)) {
            this.UPC = UPC;
            this.UPCProm = UPCProm;
            this.idProduct = idProduct;
            this.sellingPrice = sellingPrice;
            this.productsNumber = productsNumber;
            this.promotional = promotional;
        }
        else {
            throw new IllegalArgumentException("Логічна помилка: акційний статус не збігається з наявністю UPCProm!");
        }
    }

    public boolean isValid() {
        return (UPCProm==null && promotional==false) || (UPCProm!=null && promotional==true);
    }
}
