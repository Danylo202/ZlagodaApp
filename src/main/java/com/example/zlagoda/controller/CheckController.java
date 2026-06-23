package com.example.zlagoda.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.zlagoda.model.Check;
import com.example.zlagoda.repository.CheckRepository;
import com.example.zlagoda.repository.SaleRepository;

import java.time.LocalDate;

@Controller
public class CheckController {
    private final SaleRepository saleRepository;
    private final CheckRepository checkRepository;

    public CheckController(SaleRepository saleRepository, CheckRepository checkRepository) {
        this.saleRepository = saleRepository;
        this.checkRepository = checkRepository;
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
    public String newForm(Model model, Authentication auth) {
        Check check = new Check();
        check.setIdEmployee(auth.getName());
        check.setPrintDate(LocalDate.now());
        model.addAttribute("check", check);
        return "checks/new";
    }

    @PostMapping("/checks/save")
    public String saveForm(@ModelAttribute Check check, Authentication auth, RedirectAttributes ra) {
        try {
            check.setIdEmployee(auth.getName());
            if (check.getPrintDate() == null) {
                check.setPrintDate(LocalDate.now());
            }
            Integer checkId = checkRepository.save(check);
            ra.addFlashAttribute("success", "Чек додано.");
            return "redirect:/checks/" + checkId + "/details";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Помилка: " + e.getMessage());
            return "redirect:/checks/new";
        }
    }

    @GetMapping("/checks/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            checkRepository.delete(id);
            ra.addFlashAttribute("success", "Чек видалено.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Помилка при видаленні.");
        }
        return "redirect:/checks";
    }
}
