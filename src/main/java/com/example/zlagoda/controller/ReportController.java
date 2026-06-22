package com.example.zlagoda.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Placeholder controller for Reports (Звіти) pages.
 * Manager-only report/query views per AIS spec will be implemented later.
 */
@Controller
public class ReportController {

    @GetMapping("/reports")
    public String index() { return "reports/index"; }
}
