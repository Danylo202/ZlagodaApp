package com.example.zlagoda.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Placeholder controller for Customer Card (Карта клієнта) pages.
 * Manager + cashier can add/edit; manager can delete. Full CRUD per AIS spec TBD.
 */
@Controller
public class CustomerController {

    @GetMapping("/customers")
    public String list() { return "customers/list"; }

    @GetMapping("/customers/new")
    public String newForm() { return "customers/form"; }

    @GetMapping("/customers/{number}/edit")
    public String edit(@PathVariable String number) { return "customers/form"; }

    @GetMapping("/customers/{number}/delete")
    public String delete(@PathVariable String number) { return "redirect:/customers"; }

    @PostMapping("/customers/save")
    public String save() { return "redirect:/customers"; }
}
