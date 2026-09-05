package com.nirmala.hospital.repository;

import com.nirmala.hospital.model.Enquiry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnquiryRepository extends MongoRepository<Enquiry, String> {
    List<Enquiry> findByStatus(String status);
}
