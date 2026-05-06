package com.careerconnect.controller;

import com.careerconnect.entity.User;
import com.careerconnect.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/applicant/premium")
@RequiredArgsConstructor
public class PremiumController {

    private final UserService userService;

    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /** Show plans page */
    @GetMapping
    public String plansPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("user", getCurrentUser(userDetails));
        return "applicant/premium-plans";
    }

    /**
     * Simulated payment endpoint.
     * In a real app this would integrate Stripe/Razorpay.
     * Here we simply flip the premium flag.
     */
    @PostMapping("/activate")
    public String activate(@AuthenticationPrincipal UserDetails userDetails,
                           @RequestParam String plan,
                           RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);
        user.setPremium(true);
        userService.updateProfile(user);
        redirectAttributes.addFlashAttribute("successMsg",
                "🎉 You are now a Premium member! Your profile now shows the gold ring.");
        return "redirect:/applicant/dashboard";
    }

    /** Allow user to cancel / downgrade back to free */
    @PostMapping("/cancel")
    public String cancel(@AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);
        user.setPremium(false);
        userService.updateProfile(user);
        redirectAttributes.addFlashAttribute("successMsg",
                "Your premium subscription has been cancelled.");
        return "redirect:/applicant/premium";
    }
}
