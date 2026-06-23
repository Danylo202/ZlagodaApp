package com.example.zlagoda.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.zlagoda.model.*;
import com.example.zlagoda.repository.*;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final EmployeeRepository employeeRepo;
    private final CustomerRepository customerRepo;
    private final CategoryRepository categoryRepo;
    private final ProductRepository productRepo;
    private final StoreProductRepository storeProductRepo;
    private final CheckRepository checkRepo;
    private final SaleRepository saleRepo;

    public ReportController(EmployeeRepository employeeRepo,
                            CustomerRepository customerRepo,
                            CategoryRepository categoryRepo,
                            ProductRepository productRepo,
                            StoreProductRepository storeProductRepo,
                            CheckRepository checkRepo,
                            SaleRepository saleRepo) {
        this.employeeRepo = employeeRepo;
        this.customerRepo = customerRepo;
        this.categoryRepo = categoryRepo;
        this.productRepo = productRepo;
        this.storeProductRepo = storeProductRepo;
        this.checkRepo = checkRepo;
        this.saleRepo = saleRepo;
    }

    @GetMapping
    public String index() {
        return "reports/index";
    }

    // ───── Employees ─────
    @GetMapping("/employees")
    public String employees(@RequestParam(required = false) String role,
                            @RequestParam(required = false) String surname,
                            Model model) {
        List<Employee> employees;
        String title;

        if (surname != null && !surname.trim().isEmpty()) {
            employees = employeeRepo.findBySurname(surname.trim());
            title = "Контактні дані працівників за прізвищем «" + surname.trim() + "»";
            model.addAttribute("contactView", true);
        } else if ("cashier".equalsIgnoreCase(role)) {
            employees = employeeRepo.findCashiers();
            title = "Касири за прізвищем";
        } else if ("manager".equalsIgnoreCase(role)) {
            employees = employeeRepo.findManagers();
            title = "Менеджери за прізвищем";
        } else {
            employees = employeeRepo.findAll();
            title = "Усі працівники за прізвищем";
        }

        model.addAttribute("title", title);
        model.addAttribute("employees", employees);
        model.addAttribute("showContactForm", surname == null || surname.trim().isEmpty());
        model.addAttribute("surname", surname);
        return "reports/employees";
    }

    // ───── Customers ─────
    @GetMapping("/customers")
    public String customers(@RequestParam(required = false) Integer percent,
                            Model model) {
        String title;
        List<Customer> customers;

        if (percent != null) {
            customers = customerRepo.findByPercent(percent);
            title = "Клієнти зі знижкою " + percent + "%";
        } else {
            customers = customerRepo.findAll();
            title = "Усі постійні клієнти за прізвищем";
        }

        model.addAttribute("title", title);
        model.addAttribute("customers", customers);
        model.addAttribute("percent", percent);
        return "reports/customers";
    }

    // ───── Categories ─────
    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("title", "Категорії за назвою");
        model.addAttribute("categories", categoryRepo.findAll());
        return "reports/categories";
    }

    // ───── Products ─────
    @GetMapping("/products")
    public String products(@RequestParam(required = false) String categoryName,
                           Model model) {
        String title;
        List<Product> products;

        if (categoryName != null && !categoryName.trim().isEmpty()) {
            products = productRepo.findByCategoryName(categoryName.trim());
            title = "Товари категорії «" + categoryName.trim() + "»";
        } else {
            products = productRepo.findAll();
            title = "Усі товари за назвою";
        }

        model.addAttribute("title", title);
        model.addAttribute("products", products);
        model.addAttribute("categoryName", categoryName);
        return "reports/products";
    }

    // ───── Store Products ─────
    @GetMapping("/store-products")
    public String storeProducts(@RequestParam(required = false) Boolean promo,
                                Model model) {
        String title;
        List<StoreProduct> products;

        if (promo != null) {
            products = storeProductRepo.findWithFilters(null, promo, promo ? "quantity" : "name");
            title = promo ? "Акційні товари" : "Неакційні товари";
        } else {
            products = storeProductRepo.findWithFilters(null, null, "quantity");
            title = "Товари у магазині за кількістю";
        }

        model.addAttribute("title", title);
        model.addAttribute("storeProducts", products);
        model.addAttribute("promo", promo);
        return "reports/store-products";
    }

    // ───── UPC Search ─────
    @GetMapping("/store-product-by-upc")
    public String storeProductByUpc(@RequestParam(required = false) String upc,
                                    Model model) {
        if (upc != null && !upc.trim().isEmpty()) {
            List<StoreProduct> results = storeProductRepo.findByUPCWithProduct(upc.trim());
            model.addAttribute("results", results);
            model.addAttribute("upc", upc.trim());
        }
        return "reports/upc";
    }

    // ───── Checks ─────
    @GetMapping("/checks")
    public String checks(
            @RequestParam(required = false) String idEmployee,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2,
            Model model) {

        String title = "Чеки";

        if (idEmployee != null && !idEmployee.trim().isEmpty()) {
            title += " касира " + idEmployee.trim();
        } else {
            title += " усіх касирів";
        }
        if (date1 != null && date2 != null) {
            title += " за період " + date1 + " – " + date2;
        } else if (date1 != null || date2 != null) {
            title += " за вказаний період";
        }

        if (idEmployee != null || date1 != null || date2 != null) {
            List<Check> checks = checkRepo.findWithFilters(
                    idEmployee != null && !idEmployee.trim().isEmpty() ? idEmployee.trim() : null,
                    date1, date2);
            model.addAttribute("checks", checks);
        }

        model.addAttribute("title", title);
        model.addAttribute("idEmployee", idEmployee);
        model.addAttribute("date1", date1);
        model.addAttribute("date2", date2);
        return "reports/checks";
    }

    // ───── Check Sum ─────
    @GetMapping("/checks-sum")
    public String checksSum(
            @RequestParam(required = false) String idEmployee,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2,
            Model model) {

        String title;
        Double total = null;

        boolean hasEmployee = idEmployee != null && !idEmployee.trim().isEmpty();
        boolean hasDates = date1 != null && date2 != null;

        if (hasEmployee && hasDates) {
            total = checkRepo.sumByEmployeeAndPeriod(idEmployee.trim(), date1, date2);
            title = "Сума продажів касира " + idEmployee.trim() + " за період " + date1 + " – " + date2;
        } else if (hasDates) {
            total = checkRepo.sumByPeriod(date1, date2);
            title = "Сума продажів усіх касирів за період " + date1 + " – " + date2;
        } else if (hasEmployee && !hasDates) {
            title = "Сума продажів касира " + idEmployee.trim();
        } else {
            title = "Сума продажів";
        }

        model.addAttribute("title", title);
        model.addAttribute("total", total);
        model.addAttribute("idEmployee", idEmployee);
        model.addAttribute("date1", date1);
        model.addAttribute("date2", date2);
        model.addAttribute("reportType", "checks-sum");
        return "reports/aggregate";
    }

    // ───── Product Sold Quantity ─────
    @GetMapping("/product-sold-quantity")
    public String productSoldQuantity(
            @RequestParam(required = false) Integer productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2,
            Model model) {

        String title = "Кількість проданого товару";
        Integer quantity = null;
        Product product = null;

        boolean hasProduct = productId != null;
        boolean hasDates = date1 != null && date2 != null;

        if (hasProduct && hasDates) {
            quantity = saleRepo.sumQuantityByProductAndPeriod(productId, date1, date2);
            try {
                product = productRepo.findById(productId);
                title = "Кількість проданого товару «" + product.getProductName() + "» за період " + date1 + " – " + date2;
            } catch (Exception ignored) {
                title = "Кількість проданого товару #" + productId + " за період " + date1 + " – " + date2;
            }
        } else if (hasProduct && !hasDates) {
            try {
                product = productRepo.findById(productId);
                title = "Кількість проданого товару «" + product.getProductName() + "»";
            } catch (Exception ignored) {
                title = "Кількість проданого товару #" + productId;
            }
        }

        model.addAttribute("title", title);
        model.addAttribute("quantity", quantity);
        model.addAttribute("productId", productId);
        model.addAttribute("date1", date1);
        model.addAttribute("date2", date2);
        model.addAttribute("reportType", "product-quantity");
        model.addAttribute("products", productRepo.findAll());

        return "reports/aggregate";
    }
}
