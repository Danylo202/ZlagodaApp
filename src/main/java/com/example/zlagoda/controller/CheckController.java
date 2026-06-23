package com.example.zlagoda.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.zlagoda.model.Check;
import com.example.zlagoda.model.StoreProduct;
import com.example.zlagoda.repository.*;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;


import java.sql.Date;
import java.time.LocalDate;
import java.util.List;


@Controller
public class CheckController {
    private final SaleRepository saleRepository;
    private final CheckRepository checkRepository;
    private final CheckService checkService;

    public CheckController(SaleRepository saleRepository, CheckRepository checkRepo, CheckService cs) {
        this.saleRepository = saleRepository;
        this.checkRepository = checkRepo;
        this.checkService = cs;

    }

    @GetMapping("/checks")
    public String list(@RequestParam(required = false) String idEmployee,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date1,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date2,
        Model model, Authentication auth) {

        model.addAttribute("storeProducts", checkRepository.findWithFilters(idEmployee, date1, date2));
        model.addAttribute("employeeId", auth.getName());
        
        return "receipts/list";
    }

    @GetMapping("/checks/{id}/details")
    public String details(@PathVariable Integer id, Model model) {
        model.addAttribute("sales", saleRepository.findByCheckNumber(id));
        model.addAttribute("checkId", id);
        return "receipts/details";
    }

    @GetMapping("/checks/new")
    public String newForm(Model model) {
        model.addAttribute("check", new Check());
        model.addAttribute("mode", "create");
        return "checks/new";
    }

    @PostMapping("/checks/save")
    public String saveForm(@ModelAttribute Check ch, RedirectAttributes ra) {
        try {
            checkRepository.save(ch);
            ra.addFlashAttribute("success", "Чек додано.");
        }
        catch (Exception e) {
            ra.addFlashAttribute("error", "Помилка: " + e.getMessage());
            return "redirect:/store-products/new";
        }
        return "checks/new";
    }

    @GetMapping("/checks{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            checkRepository.delete(id);
            ra.addFlashAttribute("success", "Чек видалено.");
        }
        catch (Exception e) {
            ra.addFlashAttribute("error", "Помилка при видаленні.");
        }
        return "redirect:/checks";
    }

}

