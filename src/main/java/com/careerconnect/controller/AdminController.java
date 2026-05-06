package com.careerconnect.controller;

import com.careerconnect.entity.Job;
import com.careerconnect.entity.User;
import com.careerconnect.enums.Role;
import com.careerconnect.repository.*;
import com.careerconnect.service.ApplicationService;
import com.careerconnect.service.JobService;
import com.careerconnect.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final JobService jobService;
    private final ApplicationService applicationService;

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final EventRepository eventRepository;
    private final NetworkPostRepository networkPostRepository;
    private final PostCommentRepository postCommentRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("user", getCurrentUser(userDetails));
        model.addAttribute("totalUsers", userService.findAll().size());
        model.addAttribute("totalApplicants", userService.countByRole(Role.APPLICANT));
        model.addAttribute("totalEmployers", userService.countByRole(Role.EMPLOYER));
        model.addAttribute("totalJobs", jobService.countAll());
        model.addAttribute("totalApplications", applicationService.countAll());
        model.addAttribute("recentJobs", jobService.findAll().stream().limit(5).toList());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String manageUsers(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("user", getCurrentUser(userDetails));
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    @PostMapping("/users/delete/{userId}")
    public String deleteUser(@PathVariable Long userId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User current = getCurrentUser(userDetails);
        if (current.getId().equals(userId)) {
            redirectAttributes.addFlashAttribute("errorMsg", "Cannot delete your own account.");
            return "redirect:/admin/users";
        }
        userService.deleteUser(userId);
        redirectAttributes.addFlashAttribute("successMsg", "User deleted.");
        return "redirect:/admin/users";
    }

    @GetMapping("/jobs")
    public String manageJobs(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("user", getCurrentUser(userDetails));
        model.addAttribute("jobs", jobService.findAll());
        return "admin/jobs";
    }

    @PostMapping("/jobs/delete/{jobId}")
    public String deleteJob(@PathVariable Long jobId, RedirectAttributes redirectAttributes) {
        jobService.adminDeleteJob(jobId);
        redirectAttributes.addFlashAttribute("successMsg", "Job deleted.");
        return "redirect:/admin/jobs";
    }

    @PostMapping("/reset-data")
    public String resetData(@AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
        User admin = getCurrentUser(userDetails);
        
        // Wipe in order of dependencies
        postCommentRepository.deleteAll();
        networkPostRepository.deleteAll();
        applicationRepository.deleteAll();
        jobRepository.deleteAll();
        eventRepository.deleteAll();
        
        // Delete all users except the current admin
        List<User> allUsers = userRepository.findAll();
        List<User> usersToDelete = allUsers.stream()
                .filter(u -> !u.getId().equals(admin.getId()))
                .collect(Collectors.toList());
        
        userRepository.deleteAll(usersToDelete);

        redirectAttributes.addFlashAttribute("successMsg", "All data and users (except you) have been removed.");
        return "redirect:/admin/dashboard";
    }
}
