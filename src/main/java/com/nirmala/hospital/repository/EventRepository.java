package com.nirmala.hospital.repository;

import com.nirmala.hospital.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {

    // All active events ordered by date ascending (closest first)
    List<Event> findByActiveTrueOrderByEventDateAsc();

    // All events with popup enabled and active, ordered by date
    List<Event> findByActiveTrueAndPopupEnabledTrueOrderByEventDateAsc();
}
