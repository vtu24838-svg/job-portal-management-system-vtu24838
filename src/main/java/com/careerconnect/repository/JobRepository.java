package com.careerconnect.repository;

import com.careerconnect.entity.Job;
import com.careerconnect.entity.User;
import com.careerconnect.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByEmployer(User employer);
    List<Job> findByStatus(JobStatus status);
    List<Job> findByEmployerAndStatus(User employer, JobStatus status);

    @Query("SELECT j FROM Job j WHERE j.status = 'OPEN' AND " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(j.company) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:location IS NULL OR :location = '' OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:type IS NULL OR :type = '' OR j.type = :type) AND " +
           "(:category IS NULL OR :category = '' OR j.category = :category)")
    List<Job> searchJobs(@Param("keyword") String keyword,
                         @Param("location") String location,
                         @Param("type") String type,
                         @Param("category") String category);
}
