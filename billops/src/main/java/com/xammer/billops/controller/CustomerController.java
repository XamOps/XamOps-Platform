package com.xammer.billops.controller;

import com.xammer.billops.domain.Customer;
import com.xammer.billops.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.security.Principal;

@Controller
@RequestMapping("/customer") // This line is crucial
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/profile") // This line is crucial
    public String showProfile(Model model, Principal principal) {
        String username = principal.getName();
        Customer customer = customerService.findByUsername(username);

        model.addAttribute("customer", customer);
        return "customer/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute Customer customer, Principal principal) {
        customerService.updateCustomerArn(principal.getName(), customer.getAwsRoleArn());
        return "redirect:/customer/profile?success";
    }
}