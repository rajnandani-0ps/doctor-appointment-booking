package com.doctorapp.controller;

import com.doctorapp.dto.ApiResponse;
import com.doctorapp.dto.DoctorDto;
import com.doctorapp.dto.DoctorProfileUpdateDto;
import com.doctorapp.dto.SlotDto;
import com.doctorapp.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DoctorDto>>> getDoctors(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String specialization) {

        List<DoctorDto> doctors;
        if (specialization != null && !specialization.isBlank()) {
            doctors = doctorService.getDoctorsBySpecialization(specialization);
        } else if (search != null && !search.isBlank()) {
            doctors = doctorService.searchDoctors(search);
        } else {
            doctors = doctorService.getAllDoctors();
        }
        return ResponseEntity.ok(ApiResponse.ok("Doctors retrieved successfully", doctors));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DoctorDto>> getDoctorById(@PathVariable Long id) {
        try {
            DoctorDto doctor = doctorService.getDoctorById(id);
            return ResponseEntity.ok(ApiResponse.ok("Doctor details retrieved", doctor));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/specializations")
    public ResponseEntity<ApiResponse<List<String>>> getSpecializations() {
        List<String> specs = doctorService.getAllSpecializations();
        return ResponseEntity.ok(ApiResponse.ok("Specializations retrieved", specs));
    }

    @GetMapping("/{id}/slots")
    public ResponseEntity<ApiResponse<List<SlotDto>>> getDoctorSlots(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        List<SlotDto> slots = doctorService.getDoctorSlots(id, targetDate);
        return ResponseEntity.ok(ApiResponse.ok("Slots retrieved", slots));
    }

    @GetMapping("/profile/me")
    public ResponseEntity<ApiResponse<DoctorDto>> getMyDoctorProfile(Authentication authentication) {
        try {
            DoctorDto doctor = doctorService.getDoctorProfileByEmail(authentication.getName());
            return ResponseEntity.ok(ApiResponse.ok("Doctor profile retrieved", doctor));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/profile/me")
    public ResponseEntity<ApiResponse<DoctorDto>> updateMyDoctorProfile(
            Authentication authentication,
            @RequestBody DoctorProfileUpdateDto dto) {
        try {
            DoctorDto updated = doctorService.updateDoctorProfile(authentication.getName(), dto);
            return ResponseEntity.ok(ApiResponse.ok("Doctor profile updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/slots/my-slots")
    public ResponseEntity<ApiResponse<List<SlotDto>>> getMySlots(Authentication authentication) {
        try {
            DoctorDto doctor = doctorService.getDoctorProfileByEmail(authentication.getName());
            List<SlotDto> slots = doctorService.getUpcomingDoctorSlots(doctor.getId());
            return ResponseEntity.ok(ApiResponse.ok("Upcoming doctor slots retrieved", slots));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/slots")
    public ResponseEntity<ApiResponse<SlotDto>> addSlot(
            Authentication authentication,
            @Valid @RequestBody SlotDto slotDto) {
        try {
            SlotDto created = doctorService.addSlot(authentication.getName(), slotDto);
            return ResponseEntity.ok(ApiResponse.ok("Slot added successfully", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/slots/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSlot(
            Authentication authentication,
            @PathVariable Long id) {
        try {
            doctorService.deleteSlot(authentication.getName(), id);
            return ResponseEntity.ok(ApiResponse.ok("Slot deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
