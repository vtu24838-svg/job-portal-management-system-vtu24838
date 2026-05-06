package com.careerconnect.service;

import com.careerconnect.dto.EventDto;
import com.careerconnect.entity.Event;
import com.careerconnect.entity.User;
import com.careerconnect.enums.EventCategory;
import com.careerconnect.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    @Transactional
    public Event createEvent(EventDto dto, User createdBy) {
        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setEventDate(dto.getEventDate());
        event.setOrganizer(dto.getOrganizer());
        event.setCategory(dto.getCategory());
        event.setRegistrationLink(dto.getRegistrationLink());
        event.setLocation(dto.getLocation());
        event.setCreatedBy(createdBy);
        return eventRepository.save(event);
    }

    @Transactional
    public Event updateEvent(Long id, EventDto dto, User user) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setEventDate(dto.getEventDate());
        event.setOrganizer(dto.getOrganizer());
        event.setCategory(dto.getCategory());
        event.setRegistrationLink(dto.getRegistrationLink());
        event.setLocation(dto.getLocation());
        return eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }

    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }

    public List<Event> findAll() {
        return eventRepository.findAllByOrderByEventDateAsc();
    }

    public List<Event> findByCategory(EventCategory category) {
        if (category == null) return findAll();
        return eventRepository.findByCategory(category);
    }

    public List<Event> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return findAll();
        return eventRepository.searchByKeyword(keyword);
    }

    public long countAll() {
        return eventRepository.count();
    }
}
