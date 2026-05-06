package com.careerconnect.controller;

import com.careerconnect.dto.EventDto;
import com.careerconnect.entity.Event;
import com.careerconnect.entity.User;
import com.careerconnect.enums.EventCategory;
import com.careerconnect.service.EventService;
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
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final UserService userService;

    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ─── Public: Browse Events ────────────────────────────────────────────────

    @GetMapping("/events")
    public String listEvents(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam(required = false) String category,
                             @RequestParam(required = false) String keyword,
                             Model model) {
        List<Event> events;
        EventCategory selectedCategory = null;
        if (category != null && !category.isBlank()) {
            try {
                selectedCategory = EventCategory.valueOf(category.toUpperCase());
                events = eventService.findByCategory(selectedCategory);
            } catch (IllegalArgumentException e) {
                events = eventService.findAll();
            }
        } else if (keyword != null && !keyword.isBlank()) {
            events = eventService.search(keyword);
        } else {
            events = eventService.findAll();
        }

        model.addAttribute("events", events);
        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categories", EventCategory.values());
        if (userDetails != null) {
            model.addAttribute("user", getCurrentUser(userDetails));
        }
        return "events/list";
    }

    @GetMapping("/events/{id}")
    public String eventDetail(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id, Model model) {
        Event event = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        model.addAttribute("event", event);
        if (userDetails != null) {
            model.addAttribute("user", getCurrentUser(userDetails));
        }
        return "events/detail";
    }

    // ─── Employer: Create / Edit Events ───────────────────────────────────────

    @GetMapping("/employer/events/new")
    public String newEventForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("eventDto", new EventDto());
        model.addAttribute("categories", EventCategory.values());
        model.addAttribute("user", getCurrentUser(userDetails));
        return "events/form";
    }

    @PostMapping("/employer/events")
    public String createEvent(@AuthenticationPrincipal UserDetails userDetails,
                              @ModelAttribute EventDto eventDto,
                              RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);
        eventService.createEvent(eventDto, user);
        redirectAttributes.addFlashAttribute("successMsg", "Event created successfully!");
        return "redirect:/events";
    }

    @GetMapping("/employer/events/{id}/edit")
    public String editEventForm(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable Long id, Model model) {
        Event event = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        EventDto dto = new EventDto();
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate());
        dto.setOrganizer(event.getOrganizer());
        dto.setCategory(event.getCategory());
        dto.setRegistrationLink(event.getRegistrationLink());
        dto.setLocation(event.getLocation());
        model.addAttribute("eventDto", dto);
        model.addAttribute("eventId", id);
        model.addAttribute("categories", EventCategory.values());
        model.addAttribute("user", getCurrentUser(userDetails));
        return "events/form";
    }

    @PostMapping("/employer/events/{id}/edit")
    public String updateEvent(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id,
                              @ModelAttribute EventDto eventDto,
                              RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);
        eventService.updateEvent(id, eventDto, user);
        redirectAttributes.addFlashAttribute("successMsg", "Event updated!");
        return "redirect:/events";
    }

    // ─── Admin: Delete Events ─────────────────────────────────────────────────

    @PostMapping("/admin/events/{id}/delete")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        eventService.deleteEvent(id);
        redirectAttributes.addFlashAttribute("successMsg", "Event deleted.");
        return "redirect:/events";
    }

    @PostMapping("/employer/events/{id}/delete")
    public String employerDeleteEvent(@AuthenticationPrincipal UserDetails userDetails,
                                       @PathVariable Long id,
                                       RedirectAttributes redirectAttributes) {
        eventService.deleteEvent(id);
        redirectAttributes.addFlashAttribute("successMsg", "Event deleted.");
        return "redirect:/events";
    }
}
