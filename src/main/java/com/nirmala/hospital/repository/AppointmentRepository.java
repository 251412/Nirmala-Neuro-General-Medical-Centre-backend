package com.nirmala.hospital.repository;

import com.nirmala.hospital.model.Appointment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends MongoRepository<Appointment, String> {
    List<Appointment> findByDoctorId(String doctorId);
    List<Appointment> findByDepartmentId(String departmentId);
    List<Appointment> findByPreferredDate(String preferredDate);
    List<Appointment> findByPreferredDateAndDoctorId(String preferredDate, String doctorId);
    List<Appointment> findByPreferredDateAndDoctorIdAndPreferredTime(String preferredDate, String doctorId, String preferredTime);
    List<Appointment> findByEmail(String email);
    List<Appointment> findByPhone(String phone);
    java.util.Optional<Appointment> findByAppointmentId(String appointmentId);
}
