package com.nirmala.hospital.repository;

import com.nirmala.hospital.model.EmailLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailLogRepository extends MongoRepository<EmailLog, String> {
    List<EmailLog> findByAppointmentId(String appointmentId);
    List<EmailLog> findByAppointmentIdOrderByCreatedAtDesc(String appointmentId);
}
