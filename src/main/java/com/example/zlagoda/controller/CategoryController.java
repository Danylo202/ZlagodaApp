package com.example.zlagoda.controller;

import com.example.zlagoda.model.Category;
import com.example.zlagoda.model.Product;
import com.example.zlagoda.repository.CategoryRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CategoryController {
    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/categories")
    public String categories(Model model, Authentication authentication) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("employeeId", authentication.getName());
        return "categories/list";
    }

    @GetMapping("/categories/new")
    public String newCategory(Model model, Authentication authentication) {
        model.addAttribute("category", new Category());
        model.addAttribute("employeeId", authentication.getName());
        model.addAttribute("mode", "create");
        return "categories/form";
    }

    @PostMapping("/categories/save")
    public String saveProduct(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        Integer ex = categoryRepository.findByName(category.getCategoryName());
        if(ex==null) {
            categoryRepository.save(category);
            redirectAttributes.addFlashAttribute("success", "Категорію додано.");
            return "redirect:/categories";
        }
        else {
            redirectAttributes.addFlashAttribute("error", 
            "Категорія з назвою '" + category.getCategoryName() + "' вже існує!");
            return "redirect:/categories/new";
        }
    }

    @GetMapping("/categories/{id}/edit")
    public String editCategory(@PathVariable Integer id, Model model, Authentication authentication) {
        model.addAttribute("category", categoryRepository.findById(id));
        model.addAttribute("employeeId", authentication.getName());
        model.addAttribute("mode", "edit");
        return "categories/form";
    }

    @GetMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            categoryRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Категорію видалено.");
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Неможливо видалити категорію! Спочатку видаліть усі товари, що до неї належать.");
        }
        return "redirect:/categories";
    }
}
