package com.careerconnect.config;

import com.careerconnect.entity.Event;
import com.careerconnect.enums.EventCategory;
import com.careerconnect.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Seeds demo events into the database on startup if none exist.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventDataSeeder implements CommandLineRunner {

    private final EventRepository eventRepository;

    @Override
    public void run(String... args) {
        if (eventRepository.count() > 0) return; // already seeded

        log.info("[CareerConnect] Seeding demo events...");

        seed("HackFest India 2025",
             "India's largest national hackathon! 72-hour coding sprint open to all college students and graduates. " +
             "Win prizes worth ₹10L+. Themes: AI for good, sustainable tech, fintech innovation. " +
             "Team size 2-4. Pre-register now — seats are limited!",
             LocalDate.of(2025, 7, 15), "HackFest India", EventCategory.HACKATHON,
             "https://devpost.com", "Bengaluru / Online");

        seed("Google DSC Code Jam 2025",
             "Annual competitive programming contest hosted by Google Developer Student Clubs. " +
             "3 rounds of algorithmic challenges — Qualification, Round 1, and Grand Finals. " +
             "Top performers get direct Google interview fast-track. Individual participation.",
             LocalDate.of(2025, 8, 3), "Google DSC", EventCategory.CODING_COMPETITION,
             "https://codingcompetitions.withgoogle.com", "Online");

        seed("Full-Stack Dev Workshop: React + Spring Boot",
             "Intensive 2-day hands-on workshop covering modern full-stack development. " +
             "Day 1: React 18, Vite, Tailwind CSS. Day 2: Spring Boot 3, REST APIs, JPA & Hibernate. " +
             "Build and deploy a production-ready job portal from scratch. Certificate provided.",
             LocalDate.of(2025, 7, 20), "TechSkills Academy", EventCategory.WORKSHOP,
             "https://techskillsacademy.in", "Hyderabad");

        seed("AWS Cloud Practitioner Webinar",
             "Free webinar series covering AWS core services: EC2, S3, Lambda, RDS, and CloudFront. " +
             "Perfect for developers looking to get AWS certified. Q&A with AWS Solution Architects included. " +
             "Recording available after the event.",
             LocalDate.of(2025, 7, 10), "AWS India", EventCategory.WEBINAR,
             "https://aws.amazon.com/events", "Online");

        seed("Smart India Hackathon — SIH 2025",
             "Government of India's flagship hackathon initiative — SIH 2025. " +
             "Solve real problems from PSUs and government ministries across 6 themes: " +
             "healthcare, agriculture, smart cities, education, environment, and heritage. " +
             "Teams of 6. Grand prize ₹1L per winning team at national level.",
             LocalDate.of(2025, 9, 1), "AICTE / Govt. of India", EventCategory.HACKATHON,
             "https://sih.gov.in", "Pan India");

        seed("Machine Learning Bootcamp — Zero to Hero",
             "3-day intensive bootcamp covering Python, NumPy, Pandas, Scikit-learn, and PyTorch. " +
             "Build 5 real projects: sentiment analysis, image classifier, price predictor, recommendation engine, and chatbot. " +
             "Placement support included. Batch size limited to 30.",
             LocalDate.of(2025, 8, 10), "AI Pathshala", EventCategory.WORKSHOP,
             "https://aipathshala.com", "Chennai / Online");

        seed("Competitive Programming League — Season 4",
             "Weekly rated contests on Codeforces-style platform. 8-week league with cumulative scoring. " +
             "Categories: Div 1 (Expert+), Div 2 (Specialist), and Div 3 (Newbie). " +
             "Top rankers get referrals to Flipkart, Atlassian, and Uber engineering teams.",
             LocalDate.of(2025, 7, 25), "CodeSprint India", EventCategory.CODING_COMPETITION,
             "https://codesprint.in", "Online");

        seed("Future of Work Summit 2025",
             "Half-day virtual summit featuring talks from CXOs of Amazon, Microsoft, Freshworks, and Zerodha. " +
             "Topics: remote work culture, AI replacing jobs (myth vs fact), building T-shaped careers, " +
             "and salary negotiation masterclass. Networking breakout rooms post-event.",
             LocalDate.of(2025, 8, 22), "CareerConnect & NASSCOM", EventCategory.OTHER,
             "https://careerconnect.in/summit", "Online");

        seed("UI/UX Design Masterclass",
             "Learn the fundamentals of user-centered design, wireframing, prototyping in Figma, and design systems. " +
             "Case studies from top tech companies. Portfolio review session included.",
             LocalDate.of(2025, 6, 28), "Creative Minds", EventCategory.WORKSHOP,
             "https://designmasterclass.com", "Pune / Online");

        seed("Global Open Source Summit",
             "Celebrating open-source contributions! Join maintainers from Linux, Apache, and Mozilla. " +
             "Workshops on contributing to large codebases, GSoC preparation, and open-source licensing.",
             LocalDate.of(2025, 10, 5), "OpenSource Foundation", EventCategory.HACKATHON,
             "https://opensourcesummit.org", "Delhi / Online");

        seed("LeetCode 101: Cracking the Coding Interview",
             "Solving frequent interview questions on arrays, trees, graphs, and dynamic programming. " +
             "Time and space complexity analysis masterclass. Mock interview session.",
             LocalDate.of(2025, 6, 15), "AlgoExpert", EventCategory.CODING_COMPETITION,
             "https://leetcode101.com", "Online");

        log.info("[CareerConnect] Demo events seeded successfully!");
    }

    private void seed(String title, String description, LocalDate date,
                      String organizer, EventCategory category,
                      String link, String location) {
        Event e = new Event();
        e.setTitle(title);
        e.setDescription(description);
        e.setEventDate(date);
        e.setOrganizer(organizer);
        e.setCategory(category);
        e.setRegistrationLink(link);
        e.setLocation(location);
        eventRepository.save(e);
    }
}
