package com.careerconnect.controller;

import com.careerconnect.dto.NetworkPostDto;
import com.careerconnect.entity.NetworkPost;
import com.careerconnect.entity.User;
import com.careerconnect.enums.PostType;
import com.careerconnect.service.NetworkService;
import com.careerconnect.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/network")
@RequiredArgsConstructor
public class NetworkController {

    private final NetworkService networkService;
    private final UserService userService;

    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ─── Feed ─────────────────────────────────────────────────────────────────

    @GetMapping
    public String feed(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        List<NetworkPost> posts = networkService.getFeed();
        model.addAttribute("posts", posts);
        model.addAttribute("user", user);
        model.addAttribute("postTypes", PostType.values());
        model.addAttribute("newPost", new NetworkPostDto());
        // User can post if they are EMPLOYER or ADMIN (verified or not)
        boolean canPost = !user.getRole().name().equals("APPLICANT") || user.isVerified();
        model.addAttribute("canPost", canPost);
        return "network/feed";
    }

    // ─── Create Post ──────────────────────────────────────────────────────────

    @PostMapping("/post")
    public String createPost(@AuthenticationPrincipal UserDetails userDetails,
                             @ModelAttribute NetworkPostDto postDto,
                             RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);
        try {
            networkService.createPost(postDto, user);
            redirectAttributes.addFlashAttribute("successMsg", "Post published!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/network";
    }

    // ─── Like Post ────────────────────────────────────────────────────────────

    @PostMapping("/post/{id}/like")
    public String likePost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        networkService.likePost(id);
        return "redirect:/network";
    }

    // ─── Comment ──────────────────────────────────────────────────────────────

    @PostMapping("/post/{id}/comment")
    public String comment(@AuthenticationPrincipal UserDetails userDetails,
                          @PathVariable Long id,
                          @RequestParam String content,
                          RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);
        networkService.addComment(id, content, user);
        return "redirect:/network";
    }

    // ─── Delete Post ──────────────────────────────────────────────────────────

    @PostMapping("/post/{id}/delete")
    public String deletePost(@AuthenticationPrincipal UserDetails userDetails,
                             @PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);
        try {
            networkService.deletePost(id, user);
            redirectAttributes.addFlashAttribute("successMsg", "Post deleted.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/network";
    }
}
