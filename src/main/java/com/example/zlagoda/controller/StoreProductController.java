package com.example.zlagoda.controller;

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
    public String list(@RequestParam(required = false) String searchUPC, Model model, Authentication auth) {
        List<StoreProduct> list;
        try {
            if (searchUPC != null && !searchUPC.trim().isEmpty()) {
                try {
                    list = List.of(storeProductRepository.findByUPC(searchUPC.trim()));
                }
                catch (Exception e) {
                    list = List.of();
                }
            }
            else {
                list = storeProductRepository.findAll();
            }
        }
        catch (Exception e) {
            list = List.of();
            model.addAttribute("error", "Не вдалося прочитати товари у магазині. Перевірте структуру Store_Product і закрийте MS Access, якщо база відкрита. Деталі: " + e.getMessage());
        }

        model.addAttribute("storeProducts", list);
        model.addAttribute("employeeId", auth.getName());
        return "store-products/list";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        try {
            model.addAttribute("storeProduct", new StoreProduct());
            model.addAttribute("allBaseProducts", productRepository.findAll());

            List<StoreProduct> nonPromotional = storeProductRepository.findAll().stream()
                    .filter(sp -> !sp.isPromotional())
                    .toList();
            model.addAttribute("baseStoreProducts", nonPromotional);

            model.addAttribute("mode", "create");
            return "store-products/form";
        }
        catch (Exception e) {
            model.addAttribute("storeProducts", List.of());
            model.addAttribute("error", "Не вдалося відкрити форму. Перевірте структуру Store_Product/Product і закрийте MS Access, якщо база відкрита. Деталі: " + e.getMessage());
            return "store-products/list";
        }
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
            ra.addFlashAttribute("error", storeProductSaveError(e));
            return "redirect:/store-products/new";
        }
        return "redirect:/store-products";
    }

    @GetMapping("/edit/{upc}")
    public String showEditForm(@PathVariable String upc, Model model) {
        try {
            StoreProduct sp = storeProductRepository.findByUPC(upc);
            model.addAttribute("storeProduct", sp);
            model.addAttribute("allBaseProducts", productRepository.findAll());

            List<StoreProduct> nonPromotional = storeProductRepository.findAll().stream()
                    .filter(candidate -> !candidate.isPromotional() && !candidate.getUPC().equals(sp.getUPC()))
                    .toList();
            model.addAttribute("baseStoreProducts", nonPromotional);

            model.addAttribute("mode", "edit");
            return "store-products/form";
        }
        catch (Exception e) {
            model.addAttribute("storeProducts", List.of());
            model.addAttribute("error", "Не вдалося відкрити товар UPC " + upc + ". Перевірте дані та закрийте MS Access, якщо база відкрита. Деталі: " + e.getMessage());
            return "store-products/list";
        }
    }

    private String storeProductSaveError(Exception e) {
        String message = e.getMessage();
        if (message != null && message.contains("unsupported collating sort order")) {
            return "Помилка: UCanAccess не може записувати у цю Access-базу, бо вона створена з українським сортуванням текстових індексів. " +
                    "Потрібно пересоздати/скомпактувати .accdb з General/General Legacy sort order або прибрати текстові індекси з Store_Product. Деталі: " + message;
        }
        return "Помилка: " + message;
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
