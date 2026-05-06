package com.careerconnect.service;

import com.careerconnect.entity.Application;
import com.careerconnect.entity.Job;
import com.careerconnect.entity.User;
import com.careerconnect.enums.AppStatus;
import com.careerconnect.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final EmailService emailService;

    @Transactional
    public Application apply(User applicant, Job job, String coverLetter) {
        if (applicationRepository.existsByApplicantAndJob(applicant, job)) {
            throw new RuntimeException("Already applied");
        }
        Application app = new Application();
        app.setApplicant(applicant);
        app.setJob(job);
        app.setCoverLetter(coverLetter);
        app.setStatus(AppStatus.PENDING);
        return applicationRepository.save(app);
    }

    public List<Application> findByApplicant(User applicant) {
        return applicationRepository.findByApplicant(applicant);
    }

    public List<Application> findByJob(Job job) {
        return applicationRepository.findByJob(job);
    }

    public Optional<Application> findById(Long id) {
        return applicationRepository.findById(id);
    }

    @Transactional
    public void updateStatus(Long appId, AppStatus status) {
        Application app = applicationRepository.findById(appId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        
        AppStatus oldStatus = app.getStatus();
        app.setStatus(status);
        applicationRepository.save(app);

        // Allow email to be sent even if the status is already SHORTLISTED (useful for testing/resending)
        // if (oldStatus == status) return;

        String applicantEmail = app.getApplicant().getEmail();
        String applicantName  = app.getApplicant().getName();
        String jobTitle       = app.getJob().getTitle();
        String companyName    = app.getJob().getCompany() != null
                                    ? app.getJob().getCompany() : "the employer";

        if (status == AppStatus.SHORTLISTED) {
            emailService.sendShortlistNotification(applicantEmail, applicantName,
                                                   jobTitle, companyName);
        } else if (status == AppStatus.REJECTED) {
            emailService.sendRejectionNotification(applicantEmail, applicantName,
                                                   jobTitle, companyName);
        }
    }

    public boolean hasApplied(User applicant, Job job) {
        return applicationRepository.existsByApplicantAndJob(applicant, job);
    }

    public long countByApplicantAndStatus(User applicant, AppStatus status) {
        return applicationRepository.countByApplicantAndStatus(applicant, status);
    }

    public long countByEmployer(User employer) {
        return applicationRepository.countByJobEmployer(employer);
    }

    public long countByEmployerAndStatus(User employer, AppStatus status) {
        return applicationRepository.countByJobEmployerAndStatus(employer, status);
    }

    public long countAll() {
        return applicationRepository.count();
    }
}
