package com.nirmala.hospital.repository;

import com.nirmala.hospital.model.UploadedFile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UploadedFileRepository extends MongoRepository<UploadedFile, String> {
    Optional<UploadedFile> findByStoredFilename(String storedFilename);
    Optional<UploadedFile> findByImageUrl(String imageUrl);
}
