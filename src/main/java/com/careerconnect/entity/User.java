package com.careerconnect.entity;

import com.careerconnect.enums.CareerStatus;
import com.careerconnect.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(length = 20)
    private String phone;

    @Column(length = 10)
    private String gender;

    @Column(length = 150)
    private String company;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(columnDefinition = "TEXT")
    private String education;

    @Column(columnDefinition = "TEXT")
    private String experience;

    @Column(name = "resume_path")
    private String resumePath;

    @Column(name = "profile_picture")
    private String profilePicture;

    /** Student or Graduate — drives personalized dashboard */
    @Enumerated(EnumType.STRING)
    @Column(name = "career_status", length = 20)
    private CareerStatus careerStatus;

    /** True = verified HR/professional who can create network posts */
    @Column(nullable = false)
    private boolean verified = false;

    /** True = job seeker has an active premium plan (gold ring on avatar) */
    @Column(nullable = false)
    private boolean premium = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "employer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Job> postedJobs;

    @OneToMany(mappedBy = "applicant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Application> applications;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NetworkPost> posts;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

