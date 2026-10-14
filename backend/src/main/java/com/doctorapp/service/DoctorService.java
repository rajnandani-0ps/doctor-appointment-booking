package com.doctorapp.service;

import com.doctorapp.dto.DoctorDto;
import com.doctorapp.dto.DoctorProfileUpdateDto;
import com.doctorapp.dto.SlotDto;
import com.doctorapp.entity.AvailabilitySlot;
import com.doctorapp.entity.DoctorProfile;
import com.doctorapp.entity.User;
import com.doctorapp.repository.AvailabilitySlotRepository;
import com.doctorapp.repository.DoctorProfileRepository;
import com.doctorapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final UserRepository userRepository;

    public DoctorService(DoctorProfileRepository doctorProfileRepository,
                         AvailabilitySlotRepository availabilitySlotRepository,
                         UserRepository userRepository) {
        this.doctorProfileRepository = doctorProfileRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.userRepository = userRepository;
    }

    public List<DoctorDto> getAllDoctors() {
        return doctorProfileRepository.findAll()
                .stream()
                .map(DoctorDto::new)
                .collect(Collectors.toList());
    }

    public DoctorDto getDoctorById(Long id) {
        DoctorProfile profile = doctorProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));
        return new DoctorDto(profile);
    }

    public List<DoctorDto> getDoctorsBySpecialization(String specialization) {
        return doctorProfileRepository.findBySpecializationIgnoreCase(specialization)
                .stream()
                .map(DoctorDto::new)
                .collect(Collectors.toList());
    }

    public List<DoctorDto> searchDoctors(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllDoctors();
        }
        return doctorProfileRepository.searchDoctors(keyword.trim())
                .stream()
                .map(DoctorDto::new)
                .collect(Collectors.toList());
    }

    public List<String> getAllSpecializations() {
        return doctorProfileRepository.findDistinctSpecializations();
    }

    public DoctorDto getDoctorProfileByEmail(String email) {
        DoctorProfile profile = doctorProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found for email: " + email));
        return new DoctorDto(profile);
    }

    @Transactional
    public DoctorDto updateDoctorProfile(String email, DoctorProfileUpdateDto dto) {
        DoctorProfile profile = doctorProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found for email: " + email));

        User user = profile.getUser();
        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            user.setFullName(dto.getFullName());
        }
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            user.setPhone(dto.getPhone());
        }
        userRepository.save(user);

        if (dto.getSpecialization() != null) profile.setSpecialization(dto.getSpecialization());
        if (dto.getQualification() != null) profile.setQualification(dto.getQualification());
        if (dto.getExperienceYears() != null) profile.setExperienceYears(dto.getExperienceYears());
        if (dto.getConsultationFee() != null) profile.setConsultationFee(dto.getConsultationFee());
        if (dto.getClinicAddress() != null) profile.setClinicAddress(dto.getClinicAddress());
        if (dto.getBio() != null) profile.setBio(dto.getBio());
        if (dto.getProfileImageUrl() != null) profile.setProfileImageUrl(dto.getProfileImageUrl());

        DoctorProfile updated = doctorProfileRepository.save(profile);
        return new DoctorDto(updated);
    }

    public List<SlotDto> getDoctorSlots(Long doctorId, LocalDate date) {
        DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));

        List<AvailabilitySlot> slots = availabilitySlotRepository.findByDoctorIdAndSlotDateOrderByStartTimeAsc(doctorId, date);

        // If no slots exist yet for this date, dynamically generate standard business hours slots (9am - 5pm)
        if (slots.isEmpty()) {
            slots = generateDefaultSlotsForDate(doctor, date);
            slots = availabilitySlotRepository.saveAll(slots);
        }

        return slots.stream().map(SlotDto::new).collect(Collectors.toList());
    }

    public List<SlotDto> getUpcomingDoctorSlots(Long doctorId) {
        return availabilitySlotRepository.findByDoctorIdAndSlotDateGreaterThanEqualOrderBySlotDateAscStartTimeAsc(
                doctorId, LocalDate.now())
                .stream()
                .map(SlotDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public SlotDto addSlot(String doctorEmail, SlotDto slotDto) {
        DoctorProfile doctor = doctorProfileRepository.findByUserEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found for email: " + doctorEmail));

        AvailabilitySlot slot = new AvailabilitySlot(
                doctor,
                slotDto.getSlotDate(),
                slotDto.getStartTime(),
                slotDto.getEndTime()
        );
        slot = availabilitySlotRepository.save(slot);
        return new SlotDto(slot);
    }

    @Transactional
    public void deleteSlot(String doctorEmail, Long slotId) {
        DoctorProfile doctor = doctorProfileRepository.findByUserEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found for email: " + doctorEmail));

        AvailabilitySlot slot = availabilitySlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found with id: " + slotId));

        if (!slot.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException("Unauthorized: Cannot delete slot belonging to another doctor");
        }

        availabilitySlotRepository.delete(slot);
    }

    private List<AvailabilitySlot> generateDefaultSlotsForDate(DoctorProfile doctor, LocalDate date) {
        List<AvailabilitySlot> defaultSlots = new ArrayList<>();
        String[] startTimes = {"09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                               "14:00", "14:30", "15:00", "15:30", "16:00", "16:30"};
        String[] endTimes   = {"09:30", "10:00", "10:30", "11:00", "11:30", "12:00",
                               "14:30", "15:00", "15:30", "16:00", "16:30", "17:00"};

        for (int i = 0; i < startTimes.length; i++) {
            defaultSlots.add(new AvailabilitySlot(doctor, date, startTimes[i], endTimes[i]));
        }
        return defaultSlots;
    }
}
