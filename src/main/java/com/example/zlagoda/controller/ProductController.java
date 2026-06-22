package com.example.zlagoda.controller;

import com.example.zlagoda.model.Category;
import com.example.zlagoda.model.Product;
import com.example.zlagoda.repository.CategoryRepository;
import com.example.zlagoda.repository.ProductRepository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
public class ProductController {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductController(ProductRepository p, CategoryRepository c) {
        this.productRepository = p;
        this.categoryRepository = c;
    }

    // @GetMapping("/products")
    // public String products(Model model, Authentication authentication) {
    //     model.addAttribute("products", productRepository.findAll());
    //     model.addAttribute("categories", categoryRepository.findAll());
    //     return "products/products";
    // }

    // створення нового товару
    @GetMapping("/products/new")
    public String showNewProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("mode", "create");
        return "products/edit-product"; 
    }

    // збереження створеного товару
    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product) {
        Integer ex = productRepository.exists(product.getProductName(), product.getProducer());
        if (ex!=null) {
            product.setIdProduct(ex);
            productRepository.update(product); // якщо такий товар вже є
        }
        else {
            productRepository.save(product);
        }
        return "redirect:/products";
    }


    // відкриваємо для редагування
    @GetMapping("/products/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        try {
            Product product = productRepository.findById(id);
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryRepository.findAll());
            return "products/edit-product";
        }
        catch(EmptyResultDataAccessException e) {
            return "redirect:/products?error=notfound";
        }
    }

    // оброблюємо зміни
    @PostMapping("/products/update")
    public String update(@ModelAttribute Product product) {
        productRepository.update(product);
        return "redirect:/products";
    }

    @GetMapping("/products/delete/{id}")
    public String delete(@PathVariable Integer id) {
        productRepository.delete(id);
        return "redirect:/products";
    }

    @GetMapping("/products")
    public String list(@RequestParam(required = false) String catName, Model model, Authentication authentication) {
        List<Product> products;

        if (catName != null && !catName.trim().isEmpty()) {
            products = productRepository.findByCategoryName(catName);
        } 
        else {
            products = productRepository.findAll();
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryRepository.findAll());
        return "products/products";
    }
}
