package com.careerconnect.service;

import com.careerconnect.dto.UserRegistrationDto;
import com.careerconnect.entity.User;
import com.careerconnect.enums.Role;
import com.careerconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResumeSkillExtractor skillExtractor;

    private static final String UPLOAD_DIR = Paths.get("uploads", "resumes").toAbsolutePath().toString() + File.separator;
    private static final String PROFILE_DIR = Paths.get("uploads", "profiles").toAbsolutePath().toString() + File.separator;

    @Transactional
    public User registerUser(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setGender(dto.getGender());
        user.setRole(dto.getRole() != null ? dto.getRole() : Role.APPLICANT);
        if (dto.getRole() == Role.EMPLOYER) {
            user.setCompany(dto.getCompany());
        }
        // Persist career status for applicants (Student / Graduate)
        if (dto.getCareerStatus() != null && dto.getRole() == Role.APPLICANT) {
            user.setCareerStatus(dto.getCareerStatus());
        }
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }

    @Transactional
    public User updateProfile(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }

    /**
     * Saves the resume file to disk and extracts skills from it.
     * If skills are found, merges them with existing skills on the user.
     *
     * @param file   uploaded resume file (PDF or DOCX)
     * @param user   the user whose profile is being updated
     * @return the saved file name (to store as resumePath)
     */
    @Transactional
    public String saveResumeAndExtractSkills(MultipartFile file, User user) throws IOException {
        // 1. Persist file
        String fileName = saveResume(file, user.getId());

        // 2. Extract skills from the file
        String extracted = skillExtractor.extractSkills(file);
        if (extracted != null && !extracted.isBlank()) {
            // Merge with any manually entered skills
            String existing = (user.getSkills() != null && !user.getSkills().isBlank())
                    ? user.getSkills() : "";
            if (existing.isBlank()) {
                user.setSkills(extracted);
            } else {
                // Merge, deduplicate
                var merged = new java.util.LinkedHashSet<String>();
                for (String s : existing.split(",")) merged.add(s.trim());
                for (String s : extracted.split(",")) merged.add(s.trim());
                user.setSkills(String.join(", ", merged));
            }
        }

        return fileName;
    }

    public String saveResume(MultipartFile file, Long userId) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String originalName = file.getOriginalFilename();
        String extension = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf(".")) : ".pdf";
        String fileName = "resume_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    @Transactional
    public String saveProfilePicture(MultipartFile file, User user) throws IOException {
        Path uploadPath = Paths.get(PROFILE_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String originalName = file.getOriginalFilename();
        String extension = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf(".")) : ".jpg";
        
        String fileName = "avatar_" + user.getId() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
        Path filePath = uploadPath.resolve(fileName);
        System.out.println("Saving profile picture to: " + filePath.toAbsolutePath());
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        user.setProfilePicture(fileName);
        userRepository.save(user);
        System.out.println("Profile picture updated in DB for user: " + user.getEmail());
        return fileName;
    }
}

