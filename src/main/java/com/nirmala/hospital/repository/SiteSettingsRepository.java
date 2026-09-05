package com.nirmala.hospital.repository;

import com.nirmala.hospital.model.SiteSettings;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteSettingsRepository extends MongoRepository<SiteSettings, String> {
}
