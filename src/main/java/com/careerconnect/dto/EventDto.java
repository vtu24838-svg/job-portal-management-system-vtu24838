package com.careerconnect.dto;

import com.careerconnect.enums.EventCategory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter @Setter
public class EventDto {

    private String title;
    private String description;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate eventDate;

    private String organizer;
    private EventCategory category;
    private String registrationLink;
    private String location;
}
