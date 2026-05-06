package com.careerconnect.dto;

import com.careerconnect.enums.CareerStatus;
import com.careerconnect.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserRegistrationDto {

    @NotBlank(message = "Name is required")
    private String name;

    private String gender;

    @Email(message = "Enter a valid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String phone;
    private String company;
    private Role role;

    /** Career status — applicable when role = APPLICANT */
    private CareerStatus careerStatus;
}

