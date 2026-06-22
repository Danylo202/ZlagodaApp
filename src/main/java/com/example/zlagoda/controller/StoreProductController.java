package com.example.zlagoda.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Placeholder controller for Store Product (Товар у магазині) pages.
 * Full CRUD (per AIS spec) will be implemented later.
 */
@Controller
public class StoreProductController {

    @GetMapping("/store-products")
    public String list() { return "store-products/list"; }

    @GetMapping("/store-products/new")
    public String newForm() { return "store-products/form"; }

    @GetMapping("/store-products/{upc}/edit")
    public String edit(@PathVariable String upc) { return "store-products/form"; }

    @GetMapping("/store-products/{upc}/delete")
    public String delete(@PathVariable String upc) { return "redirect:/store-products"; }

    @PostMapping("/store-products/save")
    public String save() { return "redirect:/store-products"; }
}
