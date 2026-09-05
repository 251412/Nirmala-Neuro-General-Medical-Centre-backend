package com.nirmala.hospital.repository;

import com.nirmala.hospital.model.Gallery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GalleryRepository extends MongoRepository<Gallery, String> {
    List<Gallery> findByStatus(String status);
    List<Gallery> findByStatusAndCategory(String status, String category);
}
