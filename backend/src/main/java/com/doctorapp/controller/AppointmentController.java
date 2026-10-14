package com.doctorapp.controller;

import com.doctorapp.dto.ApiResponse;
import com.doctorapp.dto.AppointmentRequestDto;
import com.doctorapp.dto.AppointmentResponseDto;
import com.doctorapp.dto.AppointmentStatusUpdateDto;
import com.doctorapp.entity.Role;
import com.doctorapp.entity.User;
import com.doctorapp.service.AppointmentService;
import com.doctorapp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AuthService authService;

    public AppointmentController(AppointmentService appointmentService, AuthService authService) {
        this.appointmentService = appointmentService;
        this.authService = authService;
    }

    @PostMapping("/book")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> bookAppointment(
            Authentication authentication,
            @Valid @RequestBody AppointmentRequestDto request) {
        try {
            AppointmentResponseDto appointment = appointmentService.bookAppointment(authentication.getName(), request);
            return ResponseEntity.ok(ApiResponse.ok("Appointment booked successfully!", appointment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to book appointment: " + e.getMessage()));
        }
    }

    @GetMapping("/my-appointments")
    public ResponseEntity<ApiResponse<List<AppointmentResponseDto>>> getMyAppointments(Authentication authentication) {
        try {
            User user = authService.getUserByEmail(authentication.getName());
            List<AppointmentResponseDto> list;

            if (user.getRole() == Role.ROLE_DOCTOR) {
                list = appointmentService.getDoctorAppointments(user.getEmail());
            } else {
                list = appointmentService.getPatientAppointments(user.getEmail());
            }

            return ResponseEntity.ok(ApiResponse.ok("Appointments retrieved successfully", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> updateStatus(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody AppointmentStatusUpdateDto dto) {
        try {
            AppointmentResponseDto updated = appointmentService.updateAppointmentStatus(id, authentication.getName(), dto);
            return ResponseEntity.ok(ApiResponse.ok("Appointment status updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> cancelAppointment(
            Authentication authentication,
            @PathVariable Long id) {
        try {
            AppointmentResponseDto cancelled = appointmentService.cancelAppointment(id, authentication.getName());
            return ResponseEntity.ok(ApiResponse.ok("Appointment cancelled successfully", cancelled));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
