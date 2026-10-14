package com.doctorapp.repository;

import com.doctorapp.entity.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {
    Optional<DoctorProfile> findByUserId(Long userId);
    Optional<DoctorProfile> findByUserEmail(String email);
    List<DoctorProfile> findBySpecializationIgnoreCase(String specialization);

    @Query("SELECT DISTINCT d.specialization FROM DoctorProfile d WHERE d.specialization IS NOT NULL ORDER BY d.specialization ASC")
    List<String> findDistinctSpecializations();

    @Query("SELECT d FROM DoctorProfile d WHERE " +
           "LOWER(d.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.specialization) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.clinicAddress) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<DoctorProfile> searchDoctors(@Param("keyword") String keyword);
}
