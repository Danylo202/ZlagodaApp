package com.example.zlagoda.controller;

import com.example.zlagoda.repository.ProductRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProductController {
    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/products")
    public String products(Model model, Authentication authentication) {
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("employeeId", authentication.getName());
        return "products";
    }
}
