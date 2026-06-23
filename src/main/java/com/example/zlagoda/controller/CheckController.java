package com.example.zlagoda.controller;

import org.springframework.dao.DataAccessException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.zlagoda.model.Sale;
import com.example.zlagoda.repository.CheckRepository;
import com.example.zlagoda.repository.CheckService;
import com.example.zlagoda.repository.CustomerRepository;
import com.example.zlagoda.repository.SaleRepository;
import com.example.zlagoda.repository.StoreProductRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class CheckController {
    private final SaleRepository saleRepository;
    private final CheckRepository checkRepository;
    private final CheckService checkService;
    private final StoreProductRepository storeProductRepository;
    private final CustomerRepository customerRepository;

    public CheckController(SaleRepository saleRepository,
                           CheckRepository checkRepository,
                           CheckService checkService,
                           StoreProductRepository storeProductRepository,
                           CustomerRepository customerRepository) {
        this.saleRepository = saleRepository;
        this.checkRepository = checkRepository;
        this.checkService = checkService;
        this.storeProductRepository = storeProductRepository;
        this.customerRepository = customerRepository;
    }

    @GetMapping("/checks")
    public String list(@RequestParam(required = false) String idEmployee,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2,
                       Model model,
                       Authentication auth) {
        model.addAttribute("checks", checkRepository.findWithFilters(idEmployee, date1, date2));
        model.addAttribute("idEmployee", idEmployee);
        model.addAttribute("date1", date1);
        model.addAttribute("date2", date2);
        model.addAttribute("employeeId", auth.getName());
        return "checks/list";
    }

    @GetMapping("/checks/{id}/details")
    public String details(@PathVariable Integer id, Model model) {
        model.addAttribute("check", checkRepository.findById(id));
        model.addAttribute("sales", saleRepository.findByCheckNumber(id));
        return "checks/detail";
    }

    @GetMapping("/checks/new")
    public String newForm(Model model) {
        addCreateFormData(model);
        return "checks/new";
    }

    @PostMapping("/checks/save")
    public String saveForm(@RequestParam(required = false) String cardNumber,
                           @RequestParam(name = "upc", required = false) List<String> upcs,
                           @RequestParam(name = "quantity", required = false) List<Integer> quantities,
                           Authentication auth,
                           RedirectAttributes ra) {
        try {
            Integer checkId = checkService.createFullCheck(auth.getName(), cardNumber, requestedItems(upcs, quantities));
            ra.addFlashAttribute("success", "Чек додано. ПДВ розраховано автоматично.");
            return "redirect:/checks/" + checkId + "/details";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Помилка: " + e.getMessage());
            return "redirect:/checks/new";
        }
    }

    @GetMapping("/checks/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            checkService.deleteCheck(id);
            ra.addFlashAttribute("success", "Чек видалено.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Помилка при видаленні: " + e.getMessage());
        }
        return "redirect:/checks";
    }

    private void addCreateFormData(Model model) {
        try {
            model.addAttribute("customers", customerRepository.findAll());
        } catch (DataAccessException ex) {
            model.addAttribute("customers", Collections.emptyList());
        }

        try {
            model.addAttribute("storeProducts", storeProductRepository.findAll());
        } catch (DataAccessException ex) {
            model.addAttribute("storeProducts", Collections.emptyList());
        }
    }

    private List<Sale> requestedItems(List<String> upcs, List<Integer> quantities) {
        List<Sale> items = new ArrayList<>();
        if (upcs == null || quantities == null) {
            return items;
        }

        int count = Math.min(upcs.size(), quantities.size());
        for (int i = 0; i < count; i++) {
            Integer quantity = quantities.get(i);
            if (quantity != null && quantity > 0) {
                Sale sale = new Sale();
                sale.setUPC(upcs.get(i));
                sale.setProductNumber(quantity);
                items.add(sale);
            }
        }
        return items;
    }
}
