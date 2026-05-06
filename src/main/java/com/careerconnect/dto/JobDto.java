package com.careerconnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class JobDto {

    @NotBlank(message = "Job title is required")
    private String title;

    private String category;
    private String location;
    private String type;

    @NotBlank(message = "Description is required")
    private String description;

    private String requirements;
    private String salary;
}
