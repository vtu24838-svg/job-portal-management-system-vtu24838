package com.careerconnect.controller;

import com.careerconnect.entity.Job;
import com.careerconnect.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lightweight JSON REST API for jobs.
 * Used by the browser via fetch() to refresh the job list after an SSE event.
 */
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobApiController {

    private final JobService jobService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    /**
     * GET /api/jobs
     * Accepts same filters as the Thymeleaf browse page.
     * Returns open jobs as JSON (no sensitive data exposed).
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category) {

        List<Job> jobs = jobService.searchJobs(keyword, location, type, category);

        List<Map<String, Object>> result = jobs.stream()
                .map(this::toMap)
                .toList();

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/jobs/count
     * Returns the total number of open jobs.
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getCount() {
        long count = jobService.searchJobs(null, null, null, null).size();
        return ResponseEntity.ok(Collections.singletonMap("count", count));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Map<String, Object> toMap(Job job) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",          job.getId());
        m.put("title",       job.getTitle());
        m.put("company",     job.getCompany());
        m.put("location",    job.getLocation());
        m.put("type",        job.getType());
        m.put("category",    job.getCategory());
        m.put("salary",      job.getSalary());
        m.put("status",      job.getStatus().name());
        m.put("createdAt",   job.getCreatedAt() != null
                                 ? job.getCreatedAt().format(FMT)
                                 : "");
        return m;
    }
}
