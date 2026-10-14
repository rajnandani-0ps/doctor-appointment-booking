package com.doctorapp.service;

import com.doctorapp.dto.AppointmentRequestDto;
import com.doctorapp.dto.AppointmentResponseDto;
import com.doctorapp.dto.AppointmentStatusUpdateDto;
import com.doctorapp.entity.*;
import com.doctorapp.repository.AppointmentRepository;
import com.doctorapp.repository.AvailabilitySlotRepository;
import com.doctorapp.repository.DoctorProfileRepository;
import com.doctorapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorProfileRepository doctorProfileRepository,
                              UserRepository userRepository,
                              AvailabilitySlotRepository availabilitySlotRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.userRepository = userRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
    }

    @Transactional
    public AppointmentResponseDto bookAppointment(String patientEmail, AppointmentRequestDto request) {
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new RuntimeException("Patient user not found: " + patientEmail));

        DoctorProfile doctor = doctorProfileRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + request.getDoctorId()));

        // Check if doctor is already booked for this slot
        boolean alreadyBooked = appointmentRepository.existsByDoctorIdAndAppointmentDateAndStartTimeAndStatusNot(
                doctor.getId(),
                request.getAppointmentDate(),
                request.getStartTime(),
                AppointmentStatus.CANCELLED
        );

        if (alreadyBooked) {
            throw new IllegalArgumentException("This appointment slot is already booked. Please choose another time.");
        }

        String endTime = request.getEndTime();
        if (endTime == null || endTime.isBlank()) {
            // Default 30 min duration
            endTime = calculateEndTime(request.getStartTime());
        }

        Appointment appointment = new Appointment(
                patient,
                doctor,
                request.getAppointmentDate(),
                request.getStartTime(),
                endTime,
                request.getReason()
        );
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        Appointment saved = appointmentRepository.save(appointment);

        // Mark slot as booked in AvailabilitySlot table if exists
        Optional<AvailabilitySlot> slotOpt = availabilitySlotRepository.findByDoctorIdAndSlotDateAndStartTime(
                doctor.getId(),
                request.getAppointmentDate(),
                request.getStartTime()
        );
        if (slotOpt.isPresent()) {
            AvailabilitySlot slot = slotOpt.get();
            slot.setBooked(true);
            availabilitySlotRepository.save(slot);
        }

        return new AppointmentResponseDto(saved);
    }

    public List<AppointmentResponseDto> getPatientAppointments(String patientEmail) {
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new RuntimeException("Patient user not found: " + patientEmail));

        return appointmentRepository.findByPatientIdOrderByAppointmentDateDescStartTimeDesc(patient.getId())
                .stream()
                .map(AppointmentResponseDto::new)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponseDto> getDoctorAppointments(String doctorEmail) {
        DoctorProfile doctor = doctorProfileRepository.findByUserEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found: " + doctorEmail));

        return appointmentRepository.findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(doctor.getId())
                .stream()
                .map(AppointmentResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponseDto updateAppointmentStatus(Long appointmentId, String userEmail, AppointmentStatusUpdateDto dto) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + appointmentId));

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        boolean isDoctor = appointment.getDoctor().getUser().getId().equals(currentUser.getId());
        boolean isPatient = appointment.getPatient().getId().equals(currentUser.getId());

        if (!isDoctor && !isPatient && currentUser.getRole() != Role.ROLE_ADMIN) {
            throw new RuntimeException("Unauthorized: You cannot modify this appointment");
        }

        // Only doctors can mark completed or prescribe
        if (dto.getStatus() == AppointmentStatus.COMPLETED && !isDoctor) {
            throw new RuntimeException("Only the doctor can mark an appointment as completed");
        }

        appointment.setStatus(dto.getStatus());

        if (dto.getDoctorNotes() != null) {
            appointment.setDoctorNotes(dto.getDoctorNotes());
        }
        if (dto.getPrescription() != null) {
            appointment.setPrescription(dto.getPrescription());
        }

        // If cancelled, free up the slot
        if (dto.getStatus() == AppointmentStatus.CANCELLED) {
            freeUpSlot(appointment.getDoctor().getId(), appointment.getAppointmentDate(), appointment.getStartTime());
        }

        Appointment updated = appointmentRepository.save(appointment);
        return new AppointmentResponseDto(updated);
    }

    @Transactional
    public AppointmentResponseDto cancelAppointment(Long appointmentId, String userEmail) {
        AppointmentStatusUpdateDto dto = new AppointmentStatusUpdateDto();
        dto.setStatus(AppointmentStatus.CANCELLED);
        dto.setDoctorNotes("Cancelled by user");
        return updateAppointmentStatus(appointmentId, userEmail, dto);
    }

    private void freeUpSlot(Long doctorId, java.time.LocalDate date, String startTime) {
        Optional<AvailabilitySlot> slotOpt = availabilitySlotRepository.findByDoctorIdAndSlotDateAndStartTime(
                doctorId, date, startTime);
        slotOpt.ifPresent(slot -> {
            slot.setBooked(false);
            availabilitySlotRepository.save(slot);
        });
    }

    private String calculateEndTime(String startTime) {
        try {
            String[] parts = startTime.split(":");
            int hour = Integer.parseInt(parts[0]);
            int min = Integer.parseInt(parts[1]);
            min += 30;
            if (min >= 60) {
                min -= 60;
                hour += 1;
            }
            return String.format("%02d:%02d", hour, min);
        } catch (Exception e) {
            return startTime;
        }
    }
}
