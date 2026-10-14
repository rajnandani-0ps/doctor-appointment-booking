package com.doctorapp.repository;

import com.doctorapp.entity.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {
    List<AvailabilitySlot> findByDoctorIdAndSlotDateOrderByStartTimeAsc(Long doctorId, LocalDate slotDate);
    
    List<AvailabilitySlot> findByDoctorIdAndSlotDateGreaterThanEqualOrderBySlotDateAscStartTimeAsc(Long doctorId, LocalDate slotDate);

    Optional<AvailabilitySlot> findByDoctorIdAndSlotDateAndStartTime(Long doctorId, LocalDate slotDate, String startTime);

    void deleteByDoctorIdAndId(Long doctorId, Long id);
}
