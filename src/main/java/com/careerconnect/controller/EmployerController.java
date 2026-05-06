package com.careerconnect.controller;

import com.careerconnect.dto.JobDto;
import com.careerconnect.entity.Application;
import com.careerconnect.entity.Job;
import com.careerconnect.entity.User;
import com.careerconnect.enums.AppStatus;
import com.careerconnect.service.ApplicationService;
import com.careerconnect.service.JobService;
import com.careerconnect.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/employer")
@RequiredArgsConstructor
public class EmployerController {

    private final UserService userService;
    private final JobService jobService;
    private final ApplicationService applicationService;

    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User employer = getCurrentUser(userDetails);
        List<Job> jobs = jobService.findByEmployer(employer);
        long totalApplicants = applicationService.countByEmployer(employer);
        long shortlisted = applicationService.countByEmployerAndStatus(employer, AppStatus.SHORTLISTED);

        model.addAttribute("user", employer);
        model.addAttribute("jobs", jobs.stream().limit(5).toList());
        model.addAttribute("totalJobs", jobs.size());
        model.addAttribute("totalApplicants", totalApplicants);
        model.addAttribute("shortlisted", shortlisted);
        return "employer/dashboard";
    }

    @GetMapping("/post-job")
    public String postJobPage(Model model) {
        model.addAttribute("jobDto", new JobDto());
        return "employer/post-job";
    }

    @PostMapping("/post-job")
    public String postJob(@AuthenticationPrincipal UserDetails userDetails,
                          @Valid @ModelAttribute("jobDto") JobDto dto,
                          BindingResult result,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return "employer/post-job";
        User employer = getCurrentUser(userDetails);
        jobService.createJob(dto, employer);
        redirectAttributes.addFlashAttribute("successMsg", "Job posted successfully!");
        return "redirect:/employer/my-jobs";
    }

    @GetMapping("/my-jobs")
    public String myJobs(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User employer = getCurrentUser(userDetails);
        model.addAttribute("jobs", jobService.findByEmployer(employer));
        model.addAttribute("user", employer);
        return "employer/my-jobs";
    }

    @GetMapping("/edit-job/{jobId}")
    public String editJobPage(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long jobId, Model model) {
        User employer = getCurrentUser(userDetails);
        Job job = jobService.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));
        JobDto dto = new JobDto();
        dto.setTitle(job.getTitle());
        dto.setLocation(job.getLocation());
        dto.setType(job.getType());
        dto.setDescription(job.getDescription());
        dto.setRequirements(job.getRequirements());
        dto.setSalary(job.getSalary());
        model.addAttribute("jobDto", dto);
        model.addAttribute("jobId", jobId);
        model.addAttribute("user", employer);
        return "employer/edit-job";
    }

    @PostMapping("/edit-job/{jobId}")
    public String editJob(@AuthenticationPrincipal UserDetails userDetails,
                          @PathVariable Long jobId,
                          @Valid @ModelAttribute("jobDto") JobDto dto,
                          BindingResult result,
                          RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("jobId", jobId);
            return "employer/edit-job";
        }
        User employer = getCurrentUser(userDetails);
        jobService.updateJob(jobId, dto, employer);
        redirectAttributes.addFlashAttribute("successMsg", "Job updated successfully!");
        return "redirect:/employer/my-jobs";
    }

    @PostMapping("/delete-job/{jobId}")
    public String deleteJob(@AuthenticationPrincipal UserDetails userDetails,
                            @PathVariable Long jobId,
                            RedirectAttributes redirectAttributes) {
        User employer = getCurrentUser(userDetails);
        jobService.deleteJob(jobId, employer);
        redirectAttributes.addFlashAttribute("successMsg", "Job deleted.");
        return "redirect:/employer/my-jobs";
    }

    @PostMapping("/toggle-job/{jobId}")
    public String toggleJob(@AuthenticationPrincipal UserDetails userDetails,
                            @PathVariable Long jobId,
                            RedirectAttributes redirectAttributes) {
        User employer = getCurrentUser(userDetails);
        jobService.toggleJobStatus(jobId, employer);
        redirectAttributes.addFlashAttribute("successMsg", "Job status updated.");
        return "redirect:/employer/my-jobs";
    }

    @GetMapping("/applicants/{jobId}")
    public String viewApplicants(@AuthenticationPrincipal UserDetails userDetails,
                                 @PathVariable Long jobId, Model model) {
        User employer = getCurrentUser(userDetails);
        Job job = jobService.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));
        List<Application> applications = applicationService.findByJob(job);
        model.addAttribute("job", job);
        model.addAttribute("applications", applications);
        model.addAttribute("user", employer);
        return "employer/applicants";
    }

    @PostMapping("/application/{appId}/status")
    public String updateApplicationStatus(@PathVariable Long appId,
                                          @RequestParam String status,
                                          @RequestParam Long jobId,
                                          RedirectAttributes redirectAttributes) {
        applicationService.updateStatus(appId, AppStatus.valueOf(status));
        redirectAttributes.addFlashAttribute("successMsg", "Status updated to " + status);
        return "redirect:/employer/applicants/" + jobId;
    }

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("user", getCurrentUser(userDetails));
        return "employer/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String name,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String company,
                                @RequestParam(value = "profilePic", required = false) MultipartFile profilePic,
                                RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);
        user.setName(name);
        user.setPhone(phone);
        user.setCompany(company);

        if (profilePic != null && !profilePic.isEmpty()) {
            try {
                userService.saveProfilePicture(profilePic, user);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("errorMsg", "Profile picture upload failed.");
                return "redirect:/employer/profile";
            }
        }
        userService.updateProfile(user);
        redirectAttributes.addFlashAttribute("successMsg", "Profile updated successfully!");
        return "redirect:/employer/profile";
    }
}
