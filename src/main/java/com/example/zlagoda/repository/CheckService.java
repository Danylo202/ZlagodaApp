package com.example.zlagoda.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.zlagoda.model.Check;
import com.example.zlagoda.model.Customer;
import com.example.zlagoda.model.Sale;
import com.example.zlagoda.model.StoreProduct;

@Service
public class CheckService {
    private final CheckRepository checkRepo;
    private final SaleRepository saleRepo;
    private final StoreProductRepository storeProductRepo;
    private final CustomerRepository customerRepo;

    public CheckService(CheckRepository checkRepo,
                        SaleRepository saleRepo,
                        StoreProductRepository storeProductRepo,
                        CustomerRepository customerRepo) {
        this.checkRepo = checkRepo;
        this.saleRepo = saleRepo;
        this.storeProductRepo = storeProductRepo;
        this.customerRepo = customerRepo;
    }

    @Transactional
    public Integer createFullCheck(String idEmployee, String cardNumber, List<Sale> requestedItems) {
        if (requestedItems == null || requestedItems.isEmpty()) {
            throw new IllegalArgumentException("Додайте хоча б один товар до чеку.");
        }

        String normalizedCardNumber = normalizeCardNumber(cardNumber);
        double discountMultiplier = discountMultiplier(normalizedCardNumber);

        List<Sale> items = new ArrayList<>();
        double total = 0.0;

        for (Sale requestedItem : requestedItems) {
            if (requestedItem.getUPC() == null || requestedItem.getUPC().isBlank()
                    || requestedItem.getProductNumber() == null || requestedItem.getProductNumber() <= 0) {
                continue;
            }

            StoreProduct storeProduct = storeProductRepo.findByUPC(requestedItem.getUPC());
            if (storeProduct.getProductsNumber() == null || storeProduct.getProductsNumber() < requestedItem.getProductNumber()) {
                throw new IllegalArgumentException("Недостатньо товару на складі: " + storeProduct.getProductName());
            }

            double discountedUnitPrice = roundMoney(storeProduct.getSellingPrice() * discountMultiplier);
            items.add(new Sale(storeProduct.getUPC(), null, requestedItem.getProductNumber(), discountedUnitPrice, storeProduct.getProductName()));
            total += discountedUnitPrice * requestedItem.getProductNumber();
        }

        if (items.isEmpty()) {
            throw new IllegalArgumentException("Додайте хоча б один товар до чеку.");
        }

        total = roundMoney(total);
        Check check = new Check();
        check.setIdEmployee(idEmployee);
        check.setCardNumber(normalizedCardNumber);
        check.setPrintDate(LocalDate.now());
        check.setSumTotal(total);
        check.setVat(roundMoney(total * 0.2));

        Integer checkId = checkRepo.save(check);

        for (Sale item : items) {
            item.setCheckNumber(checkId);
            saleRepo.save(item);
            storeProductRepo.decreaseQuantity(item.getUPC(), item.getProductNumber());
        }

        return checkId;
    }

    @Transactional
    public void deleteCheck(Integer checkId) {
        saleRepo.deleteByCheckNumber(checkId);
        checkRepo.delete(checkId);
    }

    private String normalizeCardNumber(String cardNumber) {
        return cardNumber == null || cardNumber.isBlank() ? null : cardNumber.trim();
    }

    private double discountMultiplier(String cardNumber) {
        if (cardNumber == null) {
            return 1.0;
        }
        Customer customer = customerRepo.findByCardNumber(cardNumber);
        Integer percent = customer.getPercent();
        if (percent == null || percent <= 0) {
            return 1.0;
        }
        return (100.0 - percent) / 100.0;
    }

    private double roundMoney(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
