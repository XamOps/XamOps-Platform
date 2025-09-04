package com.xammer.billops.controller;

import com.xammer.billops.domain.CloudAccount;
import com.xammer.billops.domain.Customer;
import com.xammer.billops.dto.DashboardDataDto;
import com.xammer.billops.service.CostService;
import com.xammer.billops.service.CustomerService;
import com.xammer.billops.service.DashboardService;
import com.xammer.billops.service.ResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class BillingController {

    private final CustomerService customerService;
    private final DashboardService dashboardService;
    private final CostService costService;
    private final ResourceService resourceService;

    public BillingController(CustomerService customerService, DashboardService dashboardService, CostService costService, ResourceService resourceService) {
        this.customerService = customerService;
        this.dashboardService = dashboardService;
        this.costService = costService;
        this.resourceService = resourceService;
    }

    @GetMapping("/billing")
    public String showBillingPage(Model model, Principal principal) {
        Customer customer = customerService.findByUsernameWithCloudAccounts(principal.getName());
        List<CloudAccount> accounts = customer.getCloudAccounts();
        model.addAttribute("accounts", accounts);
        return "billing";
    }

    @GetMapping("/api/billing/data/{accountId}")
    @ResponseBody
    public ResponseEntity<DashboardDataDto> getBillingData(@PathVariable Long accountId,
                                                           @RequestParam(required = false) Integer year,
                                                           @RequestParam(required = false) Integer month,
                                                           Principal principal) {
        Customer customer = customerService.findByUsernameWithCloudAccounts(principal.getName());
        Optional<CloudAccount> accountOpt = customer.getCloudAccounts().stream()
                .filter(acc -> acc.getId().equals(accountId)).findFirst();

        if (accountOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        DashboardDataDto data = dashboardService.getDashboardData(accountOpt.get(), year, month);
        return ResponseEntity.ok(data);
    }

    // --- ENDPOINT for region drill-down ---
    @GetMapping("/api/billing/breakdown/regions")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getRegionBreakdown(@RequestParam Long accountId,
                                                                         @RequestParam String serviceName,
                                                                         @RequestParam(required = false) Integer year,
                                                                         @RequestParam(required = false) Integer month,
                                                                         Principal principal) {
        Customer customer = customerService.findByUsernameWithCloudAccounts(principal.getName());
        CloudAccount account = customer.getCloudAccounts().stream()
                .filter(acc -> acc.getId().equals(accountId)).findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found"));

        List<Map<String, Object>> data = costService.getCostForServiceInRegion(account, serviceName, year, month);
        return ResponseEntity.ok(data);
    }

    // --- ENDPOINT UPDATED for instance drill-down to be service-aware ---
    @GetMapping("/api/billing/breakdown/instances")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getInstanceBreakdown(@RequestParam Long accountId,
                                                                          @RequestParam String region,
                                                                          @RequestParam String serviceName, // Added serviceName
                                                                          Principal principal) {
        Customer customer = customerService.findByUsernameWithCloudAccounts(principal.getName());
        CloudAccount account = customer.getCloudAccounts().stream()
                .filter(acc -> acc.getId().equals(accountId)).findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Pass serviceName to the resource service
        List<Map<String, Object>> data = resourceService.getResourcesInRegion(account, region, serviceName);
        return ResponseEntity.ok(data);
    }
}