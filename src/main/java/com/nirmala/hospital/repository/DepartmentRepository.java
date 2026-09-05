package com.nirmala.hospital.repository;

import com.nirmala.hospital.model.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends MongoRepository<Department, String> {
    Optional<Department> findBySlug(String slug);
    List<Department> findByStatus(String status);
}
