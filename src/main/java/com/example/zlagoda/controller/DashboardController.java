package com.example.zlagoda.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    @GetMapping("/manager")
    public String managerDashboard() {
        return "manager/dashboard";
    }

    @GetMapping("/cashier")
    public String cashierDashboard() {
        return "cashier/dashboard";
    }
}
