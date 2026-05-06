package com.careerconnect.repository;

import com.careerconnect.entity.Application;
import com.careerconnect.entity.Job;
import com.careerconnect.entity.User;
import com.careerconnect.enums.AppStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByApplicant(User applicant);
    List<Application> findByJob(Job job);
    List<Application> findByApplicantAndStatus(User applicant, AppStatus status);
    Optional<Application> findByApplicantAndJob(User applicant, Job job);
    boolean existsByApplicantAndJob(User applicant, Job job);
    long countByApplicantAndStatus(User applicant, AppStatus status);
    long countByJobEmployer(User employer);
    long countByJobEmployerAndStatus(User employer, AppStatus status);
}
