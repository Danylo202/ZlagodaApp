package com.example.zlagoda.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Placeholder controller for Receipt (Чек) pages.
 * Cashier creates receipts; manager can view/delete. Full logic per AIS spec TBD.
 */
@Controller
public class ReceiptController {

    @GetMapping("/receipts")
    public String list() { return "receipts/list"; }

    @GetMapping("/receipts/new")
    public String newForm() { return "receipts/new"; }

    @GetMapping("/receipts/{number}")
    public String detail(@PathVariable String number) { return "receipts/detail"; }

    @GetMapping("/receipts/{number}/delete")
    public String delete(@PathVariable String number) { return "redirect:/receipts"; }

    @PostMapping("/receipts/save")
    public String save() { return "redirect:/receipts"; }
}
