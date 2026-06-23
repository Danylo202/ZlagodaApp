package com.example.zlagoda.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.zlagoda.model.*;

@Service
public class CheckService {
    private final CheckRepository checkRepo;
    private final SaleRepository saleRepo;
    private final StoreProductRepository storeProductRepo;

    public CheckService(CheckRepository cr, SaleRepository sr, StoreProductRepository spr) {
        this.checkRepo = cr;
        this.saleRepo = sr;
        this.storeProductRepo = spr;
    }

    @Transactional
    public void createFullCheck(Check check, List<Sale> items) {
        double total = items.stream().mapToDouble(i -> i.getSellingPrice() * i.getProductNumber()).sum();
        check.setSumTotal(total);
        check.setVat(total * 0.2);
        check.setPrintDate(LocalDate.now());

        Integer checkId = checkRepo.save(check);

        for (Sale item : items) {
            item.setCheckNumber(checkId);
            saleRepo.save(item);
            storeProductRepo.decreaseQuantity(item.getUPC(), item.getProductNumber());
        }
    }
}