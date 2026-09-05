package com.nirmala.hospital.repository;

import com.nirmala.hospital.model.EmergencyInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmergencyInfoRepository extends MongoRepository<EmergencyInfo, String> {
}
