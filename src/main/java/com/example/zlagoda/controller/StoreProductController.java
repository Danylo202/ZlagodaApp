package com.example.zlagoda.controller;

import com.example.zlagoda.model.Product;
import com.example.zlagoda.model.StoreProduct;
import com.example.zlagoda.repository.StoreProductRepository;
import com.example.zlagoda.repository.ProductRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/store-products")
public class StoreProductController {

    private final StoreProductRepository storeProductRepository;
    private final ProductRepository productRepository;

    public StoreProductController(StoreProductRepository spRepo, ProductRepository pRepo) {
        this.storeProductRepository = spRepo;
        this.productRepository = pRepo;
    }

    @GetMapping
    public String list(
        @RequestParam(required = false) String upc,
        @RequestParam(required = false) Boolean promo,
        @RequestParam(required = false, defaultValue = "name") String sort,
        Model model, Authentication auth) {

        model.addAttribute("storeProducts", storeProductRepository.findWithFilters(upc, promo, sort));
        model.addAttribute("employeeId", auth.getName());
        return "store-products/list";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("storeProduct", new StoreProduct());
        model.addAttribute("allBaseProducts", productRepository.findAll());
        
        List<StoreProduct> nonPromotional = storeProductRepository.findAll().stream()
                .filter(sp -> !sp.isPromotionalProduct())
                .toList();
        model.addAttribute("baseStoreProducts", nonPromotional);
        
        model.addAttribute("mode", "create");
        return "store-products/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute StoreProduct sp, RedirectAttributes ra) {
        try {
            if (storeProductRepository.exists(sp.getUPC())) {
                storeProductRepository.update(sp);
                ra.addFlashAttribute("success", "Дані оновлено.");
            }
            else {
                storeProductRepository.save(sp);
                ra.addFlashAttribute("success", "Товар додано на полицю.");
            }
        }
        catch (Exception e) {
            ra.addFlashAttribute("error", "Помилка: " + e.getMessage());
            return "redirect:/store-products/new";
        }
        return "redirect:/store-products";
    }

    @GetMapping("/edit/{upc}")
    public String showEditForm(@PathVariable String upc, Model model) {
        StoreProduct sp = storeProductRepository.findByUPC(upc);
        model.addAttribute("storeProduct", sp);
        model.addAttribute("mode", "edit");
        return "store-products/form";
    }

    @PostMapping("/store-products/update")
    public String update(@ModelAttribute StoreProduct sp) {
        storeProductRepository.update(sp);
        return "redirect:/store-products";
    }

    @PostMapping("/delete/{upc}")
    public String delete(@PathVariable String upc, RedirectAttributes ra) {
        try {
            storeProductRepository.delete(upc);
            ra.addFlashAttribute("success", "Товар прибрано з полиці.");
        }
        catch (Exception e) {
            ra.addFlashAttribute("error", "Неможливо видалити: товар фігурує в чеках.");
        }
        return "redirect:/store-products";
    }
}
