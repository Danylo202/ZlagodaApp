package com.example.zlagoda.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    @GetMapping("/manager")
    public String managerDashboard(Model model, Authentication authentication) {
        model.addAttribute("employeeId", authentication.getName());
        return "manager/dashboard";
    }

    @GetMapping("/cashier")
    public String cashierDashboard(Model model, Authentication authentication) {
        model.addAttribute("employeeId", authentication.getName());
        return "cashier/dashboard";
    }
}
