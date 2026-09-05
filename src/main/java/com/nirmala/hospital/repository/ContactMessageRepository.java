package com.nirmala.hospital.repository;

import com.nirmala.hospital.model.ContactMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactMessageRepository extends MongoRepository<ContactMessage, String> {
    List<ContactMessage> findByStatus(String status);
}
