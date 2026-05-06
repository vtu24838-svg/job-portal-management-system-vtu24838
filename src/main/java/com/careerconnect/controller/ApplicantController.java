package com.careerconnect.controller;

import com.careerconnect.entity.Application;
import com.careerconnect.entity.Job;
import com.careerconnect.entity.User;
import com.careerconnect.enums.AppStatus;
import com.careerconnect.enums.CareerStatus;
import com.careerconnect.service.ApplicationService;
import com.careerconnect.service.EventService;
import com.careerconnect.service.JobService;
import com.careerconnect.service.JobService.JobRecommendation;
import com.careerconnect.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/applicant")
@RequiredArgsConstructor
public class ApplicantController {

    private final UserService userService;
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final EventService eventService;

    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ─── Dashboard ──────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        List<Application> applications = applicationService.findByApplicant(user);
        long pending     = applicationService.countByApplicantAndStatus(user, AppStatus.PENDING);
        long shortlisted = applicationService.countByApplicantAndStatus(user, AppStatus.SHORTLISTED);
        long rejected    = applicationService.countByApplicantAndStatus(user, AppStatus.REJECTED);

        // Profile strength (0–100)
        int strength = 20;
        if (user.getPhone()     != null && !user.getPhone().isEmpty())     strength += 15;
        if (user.getSkills()    != null && !user.getSkills().isEmpty())    strength += 20;
        if (user.getEducation() != null && !user.getEducation().isEmpty()) strength += 20;
        if (user.getExperience()!= null && !user.getExperience().isEmpty())strength += 15;
        if (user.getResumePath()!= null && !user.getResumePath().isEmpty())strength += 10;

        // Skill-based recommended jobs (top 4)
        List<JobRecommendation> recommendations = jobService.getRecommendedJobs(user)
                .stream().limit(4).collect(Collectors.toList());

        // IDs already recommended — exclude from recent jobs
        Set<Long> recommendedIds = recommendations.stream()
                .map(r -> r.job().getId()).collect(Collectors.toSet());

        // Career-status personalized job filtering
        boolean isStudent = CareerStatus.STUDENT.equals(user.getCareerStatus());
        String preferredType = isStudent ? "Internship" : "Full-time";

        List<Job> recentJobs = jobService.searchJobs(null, null, null, null)
                .stream()
                .filter(j -> !recommendedIds.contains(j.getId()))
                .filter(j -> isStudent
                        ? (j.getType() != null &&
                           (j.getType().equalsIgnoreCase("Internship") ||
                            j.getType().equalsIgnoreCase("Entry Level")))
                        : true)
                .limit(6)
                .collect(Collectors.toList());

        // Upcoming events (hackathons for students, workshops for graduates)
        var events = eventService.findAll().stream()
                .filter(e -> {
                    if (isStudent) {
                        return e.getCategory() != null &&
                               (e.getCategory().name().equals("HACKATHON") ||
                                e.getCategory().name().equals("CODING_COMPETITION"));
                    }
                    return true;
                })
                .limit(3)
                .collect(Collectors.toList());

        model.addAttribute("user", user);
        model.addAttribute("applications", applications.stream().limit(5).toList());
        model.addAttribute("totalApplied", applications.size());
        model.addAttribute("pending", pending);
        model.addAttribute("shortlisted", shortlisted);
        model.addAttribute("rejected", rejected);
        model.addAttribute("profileStrength", strength);
        model.addAttribute("recentJobs", recentJobs);
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("isStudent", isStudent);
        model.addAttribute("preferredType", preferredType);
        model.addAttribute("upcomingEvents", events);
        return "applicant/dashboard";
    }

    // ─── Profile ─────────────────────────────────────────────────────────────

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("user", getCurrentUser(userDetails));
        return "applicant/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String name,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String skills,
                                @RequestParam(required = false) String education,
                                @RequestParam(required = false) String experience,
                                @RequestParam(value = "resume", required = false) MultipartFile resume,
                                @RequestParam(value = "profilePic", required = false) MultipartFile profilePic,
                                RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);
        user.setName(name);
        user.setPhone(phone);
        user.setSkills(skills);
        user.setEducation(education);
        user.setExperience(experience);

        // Handle Profile Picture
        if (profilePic != null && !profilePic.isEmpty()) {
            try {
                userService.saveProfilePicture(profilePic, user);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("errorMsg", "Profile picture upload failed.");
                return "redirect:/applicant/profile";
            }
        }

        // Handle Resume
        if (resume != null && !resume.isEmpty()) {
            // Validate file type
            String originalName = resume.getOriginalFilename();
            if (originalName != null &&
                !originalName.toLowerCase().endsWith(".pdf") &&
                !originalName.toLowerCase().endsWith(".docx")) {
                redirectAttributes.addFlashAttribute("errorMsg", "Only PDF or DOCX files are accepted.");
                return "redirect:/applicant/profile";
            }
            try {
                // Save resume and auto-extract skills
                String path = userService.saveResumeAndExtractSkills(resume, user);
                user.setResumePath(path);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("errorMsg", "Resume upload failed.");
                return "redirect:/applicant/profile";
            }
        }
        userService.updateProfile(user);
        redirectAttributes.addFlashAttribute("successMsg", "Profile updated! Skills auto-extracted from resume.");
        return "redirect:/applicant/profile";
    }

    // ─── Browse Jobs ──────────────────────────────────────────────────────────

    @GetMapping("/jobs")
    public String browseJobs(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String location,
                             @RequestParam(required = false) String type,
                             @RequestParam(required = false) String category,
                             Model model) {
        User user = getCurrentUser(userDetails);

        // Recommendations first (when no filter active)
        List<JobRecommendation> recommendations = List.of();
        Set<Long> recommendedIds = Set.of();
        if (keyword == null && location == null && type == null && category == null) {
            recommendations = jobService.getRecommendedJobs(user).stream()
                    .limit(6).collect(Collectors.toList());
            recommendedIds = recommendations.stream()
                    .map(r -> r.job().getId()).collect(Collectors.toSet());
        }

        // Regular filtered jobs (exclude already-recommended)
        final Set<Long> finalRecommendedIds = recommendedIds;
        List<Job> jobs = jobService.searchJobs(keyword, location, type, category)
                .stream()
                .filter(j -> !finalRecommendedIds.contains(j.getId()))
                .collect(Collectors.toList());

        model.addAttribute("jobs", jobs);
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("user", user);
        model.addAttribute("keyword", keyword);
        model.addAttribute("location", location);
        model.addAttribute("type", type);
        model.addAttribute("category", category);
        model.addAttribute("hasResume", user.getResumePath() != null && !user.getResumePath().isBlank());
        return "applicant/browse-jobs";
    }

    // ─── Job Detail ───────────────────────────────────────────────────────────

    @GetMapping("/jobs/{jobId}")
    public String viewJob(@AuthenticationPrincipal UserDetails userDetails,
                          @PathVariable Long jobId, Model model) {
        User user = getCurrentUser(userDetails);
        Job job = jobService.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));
        boolean alreadyApplied = applicationService.hasApplied(user, job);
        boolean hasResume = user.getResumePath() != null && !user.getResumePath().isBlank();
        model.addAttribute("job", job);
        model.addAttribute("user", user);
        model.addAttribute("alreadyApplied", alreadyApplied);
        model.addAttribute("hasResume", hasResume);
        return "applicant/job-detail";
    }

    // ─── Apply ────────────────────────────────────────────────────────────────

    @PostMapping("/apply/{jobId}")
    public String apply(@AuthenticationPrincipal UserDetails userDetails,
                        @PathVariable Long jobId,
                        @RequestParam(required = false) String coverLetter,
                        RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);

        // MANDATORY: resume must be uploaded before applying
        if (user.getResumePath() == null || user.getResumePath().isBlank()) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "You must upload your resume (PDF/DOCX) before applying. " +
                    "Please update your profile first.");
            return "redirect:/applicant/profile";
        }

        Job job = jobService.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));
        try {
            applicationService.apply(user, job, coverLetter);
            redirectAttributes.addFlashAttribute("successMsg", "Application submitted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/applicant/applications";
    }

    // ─── My Applications ─────────────────────────────────────────────────────

    @GetMapping("/applications")
    public String myApplications(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        List<Application> applications = applicationService.findByApplicant(user);
        model.addAttribute("applications", applications);
        model.addAttribute("user", user);
        return "applicant/my-applications";
    }
}

