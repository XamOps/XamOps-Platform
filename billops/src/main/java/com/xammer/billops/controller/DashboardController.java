package com.xammer.billops.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard() {
        // This controller now only returns the view name.
        // All data loading has been moved to the BillingController.
        return "dashboard";
    }
}