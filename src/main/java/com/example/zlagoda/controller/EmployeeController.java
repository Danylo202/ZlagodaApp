package com.example.zlagoda.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Placeholder controller for Employee (Працівник) pages.
 * Full CRUD (per AIS spec) will be implemented later.
 */
@Controller
public class EmployeeController {

    @GetMapping("/employees")
    public String list() { return "employees/list"; }

    @GetMapping("/employees/new")
    public String newForm() { return "employees/form"; }

    @GetMapping("/employees/{id}/edit")
    public String edit(@PathVariable String id) { return "employees/form"; }

    @GetMapping("/employees/{id}/delete")
    public String delete(@PathVariable String id) { return "redirect:/employees"; }

    @PostMapping("/employees/save")
    public String save() { return "redirect:/employees"; }
}
