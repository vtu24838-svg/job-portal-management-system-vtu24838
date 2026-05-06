package com.careerconnect.service;

import com.careerconnect.config.SseEmitterRegistry;
import com.careerconnect.dto.JobDto;
import com.careerconnect.entity.Job;
import com.careerconnect.entity.User;
import com.careerconnect.enums.JobStatus;
import com.careerconnect.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final SseEmitterRegistry sseRegistry;

    /** Simple record carrying a Job with its recommendation match score */
    public record JobRecommendation(Job job, int matchPercent) {}

    @Transactional
    public Job createJob(JobDto dto, User employer) {
        Job job = new Job();
        job.setTitle(dto.getTitle());
        job.setCategory(dto.getCategory());
        job.setCompany(employer.getCompany() != null ? employer.getCompany() : employer.getName());
        job.setLocation(dto.getLocation());
        job.setType(dto.getType());
        job.setDescription(dto.getDescription());
        job.setRequirements(dto.getRequirements());
        job.setSalary(dto.getSalary());
        job.setStatus(JobStatus.OPEN);
        job.setEmployer(employer);
        Job saved = jobRepository.save(job);
        sseRegistry.broadcastJobUpdate("refresh");
        return saved;
    }

    @Transactional
    public Job updateJob(Long jobId, JobDto dto, User employer) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        if (!job.getEmployer().getId().equals(employer.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        job.setTitle(dto.getTitle());
        job.setCategory(dto.getCategory());
        job.setLocation(dto.getLocation());
        job.setType(dto.getType());
        job.setDescription(dto.getDescription());
        job.setRequirements(dto.getRequirements());
        job.setSalary(dto.getSalary());
        Job saved = jobRepository.save(job);
        sseRegistry.broadcastJobUpdate("refresh");
        return saved;
    }

    @Transactional
    public void deleteJob(Long jobId, User employer) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        if (!job.getEmployer().getId().equals(employer.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        jobRepository.delete(job);
        sseRegistry.broadcastJobUpdate("refresh");
    }

    @Transactional
    public void toggleJobStatus(Long jobId, User employer) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        if (!job.getEmployer().getId().equals(employer.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        job.setStatus(job.getStatus() == JobStatus.OPEN ? JobStatus.CLOSED : JobStatus.OPEN);
        jobRepository.save(job);
        sseRegistry.broadcastJobUpdate("refresh");
    }

    public Optional<Job> findById(Long id) {
        return jobRepository.findById(id);
    }

    public List<Job> findAll() {
        return jobRepository.findAll();
    }

    public List<Job> findOpenJobs() {
        return jobRepository.findByStatus(JobStatus.OPEN);
    }

    public List<Job> findByEmployer(User employer) {
        return jobRepository.findByEmployer(employer);
    }

    public List<Job> searchJobs(String keyword, String location, String type, String category) {
        return jobRepository.searchJobs(keyword, location, type, category);
    }

    public long countAll() {
        return jobRepository.count();
    }

    @Transactional
    public void adminDeleteJob(Long jobId) {
        jobRepository.deleteById(jobId);
    }

    /**
     * Skill-based job recommendation engine.
     * Computes a match percentage for each open job based on the user's skills.
     * Returns only jobs with matchPercent > 0, sorted descending.
     *
     * Algorithm:
     *   userSkills  = set of skills from user.skills (comma-separated)
     *   jobText     = job.requirements + " " + job.title (lowercased)
     *   matches     = count of userSkills contained in jobText
     *   score       = (matches / userSkills.size) * 100
     */
    public List<JobRecommendation> getRecommendedJobs(User user) {
        if (user.getSkills() == null || user.getSkills().isBlank()) {
            return Collections.emptyList();
        }

        Set<String> userSkills = Arrays.stream(user.getSkills().split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (userSkills.isEmpty()) return Collections.emptyList();

        List<Job> openJobs = jobRepository.findByStatus(JobStatus.OPEN);

        return openJobs.stream()
                .map(job -> {
                    String jobText = ((job.getRequirements() != null ? job.getRequirements() : "") +
                                     " " + job.getTitle() +
                                     " " + (job.getDescription() != null ? job.getDescription() : "")
                                    ).toLowerCase();
                    long matches = userSkills.stream().filter(jobText::contains).count();
                    int percent = (int) ((matches * 100) / userSkills.size());
                    return new JobRecommendation(job, percent);
                })
                .filter(r -> r.matchPercent() > 0)
                .sorted(Comparator.comparingInt(JobRecommendation::matchPercent).reversed())
                .collect(Collectors.toList());
    }
}

