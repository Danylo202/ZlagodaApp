package com.example.zlagoda.controller;

import com.example.zlagoda.repository.CategoryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    private final CategoryRepository categoryRepository;

    public MainController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return "index";
    }
}
