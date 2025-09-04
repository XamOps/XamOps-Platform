package com.xammer.billops.controller;

import com.xammer.billops.domain.CloudAccount;
import com.xammer.billops.domain.Customer;
import com.xammer.billops.dto.AccountCreationRequestDto;
import com.xammer.billops.dto.GcpAccountRequestDto;
import com.xammer.billops.dto.VerifyAccountRequest;
import com.xammer.billops.repository.CloudAccountRepository;
import com.xammer.billops.service.AwsAccountService;
import com.xammer.billops.service.CustomerService;
import com.xammer.billops.service.GcpDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/account")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class AccountManagerController {

    private final AwsAccountService awsAccountService;
    private final GcpDataService gcpDataService;
    private final CustomerService customerService;
    private final CloudAccountRepository cloudAccountRepository;

    public AccountManagerController(AwsAccountService awsAccountService, GcpDataService gcpDataService,
                                    CustomerService customerService, CloudAccountRepository cloudAccountRepository) {
        this.awsAccountService = awsAccountService;
        this.gcpDataService = gcpDataService;
        this.customerService = customerService;
        this.cloudAccountRepository = cloudAccountRepository;
    }
    @GetMapping("/manager")
    public String showAccountManager(Model model, Principal principal) {
        // --- UPDATED TO USE THE NEW METHOD ---
        Customer customer = customerService.findByUsernameWithCloudAccounts(principal.getName());
        List<CloudAccount> accounts = customer.getCloudAccounts();
        model.addAttribute("accounts", accounts);
        return "account-manager";
    }

    @GetMapping("/add")
    public String showAddAccountPage() {
        return "add-account";
    }

    @GetMapping("/add-gcp")
    public String showAddGcpAccountPage(Model model) {
        model.addAttribute("gcpAccountRequest", new GcpAccountRequestDto());
        return "add-gcp-account";
    }

    @PostMapping("/add-gcp")
    public String addGcpAccount(@ModelAttribute("gcpAccountRequest") GcpAccountRequestDto request,
                                Principal principal, RedirectAttributes redirectAttributes) {
        try {
            Customer customer = customerService.findByUsername(principal.getName());
            gcpDataService.createGcpAccount(request, customer);
            redirectAttributes.addFlashAttribute("successMessage", "GCP account added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add GCP account: " + e.getMessage());
        }
        return "redirect:/account/manager";
    }

    @PostMapping("/generate-stack-url")
    @ResponseBody
    public ResponseEntity<Map<String, String>> generateStackUrl(@RequestBody AccountCreationRequestDto request, Principal principal) {
        Customer customer = customerService.findByUsername(principal.getName());
        String url = awsAccountService.generateCloudFormationUrl(request.getAccountName(), customer);

        Map<String, String> response = new HashMap<>();
        response.put("url", url);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public String verifyAccount(VerifyAccountRequest request, RedirectAttributes redirectAttributes) {
        try {
            awsAccountService.verifyAccount(request);
            redirectAttributes.addFlashAttribute("successMessage", "Account verified successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to verify account: " + e.getMessage());
        }
        return "redirect:/account/manager";
    }

    @PostMapping("/delete/{id}")
    public String deleteAccount(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            cloudAccountRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Account removed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to remove account: " + e.getMessage());
        }
        return "redirect:/account/manager";
    }
}