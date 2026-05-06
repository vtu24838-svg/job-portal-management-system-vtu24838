package com.careerconnect.repository;

import com.careerconnect.entity.Event;
import com.careerconnect.enums.EventCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByCategory(EventCategory category);

    List<Event> findAllByOrderByEventDateAsc();

    @Query("SELECT e FROM Event e WHERE " +
           "(:category IS NULL OR e.category = :category) " +
           "ORDER BY e.eventDate ASC")
    List<Event> findByOptionalCategory(@Param("category") EventCategory category);

    @Query("SELECT e FROM Event e WHERE " +
           "LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.organizer) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Event> searchByKeyword(@Param("keyword") String keyword);
}
