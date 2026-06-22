package com.example.zlagoda.controller;

import com.example.zlagoda.model.Category;
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

    @PostMapping("/categories")
    public String createCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        categoryRepository.save(category);
        redirectAttributes.addFlashAttribute("success", "Категорію додано.");
        return "redirect:/categories";
    }

    @GetMapping("/categories/{id}/edit")
    public String editCategory(@PathVariable Integer id, Model model, Authentication authentication) {
        model.addAttribute("category", categoryRepository.findById(id));
        model.addAttribute("employeeId", authentication.getName());
        model.addAttribute("mode", "edit");
        return "categories/form";
    }

    @PostMapping("/categories/{id}")
    public String updateCategory(@PathVariable Integer id,
                                 @ModelAttribute Category category,
                                 RedirectAttributes redirectAttributes) {
        category.setCategoryNumber(id);
        categoryRepository.update(category);
        redirectAttributes.addFlashAttribute("success", "Категорію оновлено.");
        return "redirect:/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        categoryRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Категорію видалено.");
        return "redirect:/categories";
    }
}
