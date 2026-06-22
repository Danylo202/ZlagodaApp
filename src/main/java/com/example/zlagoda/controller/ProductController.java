package com.example.zlagoda.controller;

import com.example.zlagoda.model.Product;
import com.example.zlagoda.repository.CategoryRepository;
import com.example.zlagoda.repository.ProductRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ProductController {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductController(ProductRepository p, CategoryRepository c) {
        this.productRepository = p;
        this.categoryRepository = c;
    }

    @GetMapping("/products")
    public String products(Model model, Authentication authentication) {
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        return "products";
    }

    @PostMapping("/products/add")
    public String add(@ModelAttribute Product product) {
        productRepository.save(product);
        return "redirect:/products";
    }

    @GetMapping("/products/delete/{id}")
    public String delete(@PathVariable Integer id) {
        productRepository.delete(id);
        return "redirect:/products";
    }
}
