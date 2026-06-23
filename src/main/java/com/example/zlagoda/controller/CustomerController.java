package com.example.zlagoda.controller;

import com.example.zlagoda.model.Customer;
import com.example.zlagoda.repository.CustomerRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
public class CustomerController {
    private static final String MODE_CREATE = "create";
    private static final String MODE_EDIT = "edit";

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/customers")
    public String list(@RequestParam(required = false) String surname,
                       @RequestParam(required = false) Integer percent,
                       Model model) {
        String trimmedSurname = surname == null ? null : surname.trim();
        boolean hasSurname = trimmedSurname != null && !trimmedSurname.isEmpty();

        List<Customer> customers;
        if (hasSurname && percent != null) {
            customers = customerRepository.findBySurnameAndPercent(trimmedSurname, percent);
        } else if (hasSurname) {
            customers = customerRepository.findBySurname(trimmedSurname);
        } else if (percent != null) {
            customers = customerRepository.findByPercent(percent);
        } else {
            customers = customerRepository.findAll();
        }

        model.addAttribute("customers", customers);
        model.addAttribute("surname", surname);
        model.addAttribute("percent", percent);
        return "customers/list";
    }

    @GetMapping("/customers/new")
    public String newForm(Model model) {
        addFormAttributes(model, new Customer(), MODE_CREATE);
        return "customers/form";
    }

    @GetMapping("/customers/{number}/edit")
    public String edit(@PathVariable String number, Model model, RedirectAttributes redirectAttributes) {
        try {
            addFormAttributes(model, customerRepository.findByCardNumber(number), MODE_EDIT);
            return "customers/form";
        } catch (EmptyResultDataAccessException ex) {
            redirectAttributes.addFlashAttribute("error", "Клієнта не знайдено.");
            return "redirect:/customers";
        }
    }

    @GetMapping("/customers/{number}/delete")
    public String delete(@PathVariable String number, RedirectAttributes redirectAttributes) {
        try {
            customerRepository.deleteByCardNumber(number);
            redirectAttributes.addFlashAttribute("success", "Карту клієнта видалено.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Неможливо видалити карту клієнта: існують пов'язані чеки або інші дані.");
        }
        return "redirect:/customers";
    }

    @PostMapping("/customers/save")
    public String save(@ModelAttribute Customer customer,
                       @RequestParam(defaultValue = MODE_CREATE) String mode,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        boolean create = MODE_CREATE.equals(mode);
        List<String> errors = validate(customer);

        if (create && customerRepository.existsByCardNumber(customer.getCardNumber())) {
            errors.add("Карта клієнта з таким номером вже існує.");
        }

        if (!errors.isEmpty()) {
            addFormAttributes(model, customer, mode);
            model.addAttribute("errors", errors);
            return "customers/form";
        }

        try {
            if (create) {
                customerRepository.save(customer);
                redirectAttributes.addFlashAttribute("success", "Карту клієнта додано.");
            } else {
                customerRepository.update(customer);
                redirectAttributes.addFlashAttribute("success", "Дані клієнта оновлено.");
            }
        } catch (Exception ex) {
            addFormAttributes(model, customer, mode);
            model.addAttribute("errors", List.of("Не вдалося зберегти клієнта: " + ex.getMessage()));
            return "customers/form";
        }

        return "redirect:/customers";
    }

    private List<String> validate(Customer customer) {
        List<String> errors = new ArrayList<>();
        if (isBlank(customer.getCardNumber())) errors.add("Вкажіть номер карти.");
        if (isBlank(customer.getCustSurname())) errors.add("Вкажіть прізвище.");
        if (isBlank(customer.getCustName())) errors.add("Вкажіть ім'я.");
        if (isBlank(customer.getPhoneNumber())) errors.add("Вкажіть телефон.");
        if (customer.getPhoneNumber() != null && customer.getPhoneNumber().length() > 13) errors.add("Телефон не може перевищувати 13 символів.");
        if (customer.getPercent() == null || customer.getPercent() < 0) errors.add("Відсоток знижки не може бути від'ємним.");
        return errors;
    }

    private void addFormAttributes(Model model, Customer customer, String mode) {
        model.addAttribute("customer", customer);
        model.addAttribute("mode", mode);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
