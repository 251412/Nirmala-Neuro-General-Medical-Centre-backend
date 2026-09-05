package com.nirmala.hospital.repository;

import com.nirmala.hospital.model.Doctor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends MongoRepository<Doctor, String> {
    List<Doctor> findByStatus(String status);
    List<Doctor> findByDepartmentId(String departmentId);
    List<Doctor> findByDepartmentIdAndStatus(String departmentId, String status);
    
    @Query("{ $or: [ { 'name': { $regex: ?0, $options: 'i' } }, { 'specialization': { $regex: ?0, $options: 'i' } }, { 'qualification': { $regex: ?0, $options: 'i' } } ], 'status': 'ACTIVE' }")
    List<Doctor> searchActiveDoctors(String query);
    
    @Query("{ $or: [ { 'name': { $regex: ?0, $options: 'i' } }, { 'specialization': { $regex: ?0, $options: 'i' } }, { 'qualification': { $regex: ?0, $options: 'i' } } ], 'departmentId': ?1, 'status': 'ACTIVE' }")
    List<Doctor> searchActiveDoctorsInDepartment(String query, String departmentId);
}
