package com.doctorapp.controller;

import com.doctorapp.dto.ApiResponse;
import com.doctorapp.entity.PatientProfile;
import com.doctorapp.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/profile/me")
    public ResponseEntity<ApiResponse<PatientProfile>> getMyProfile(Authentication authentication) {
        try {
            PatientProfile profile = patientService.getPatientProfile(authentication.getName());
            return ResponseEntity.ok(ApiResponse.ok("Patient profile retrieved", profile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/profile/me")
    public ResponseEntity<ApiResponse<PatientProfile>> updateMyProfile(
            Authentication authentication,
            @RequestBody PatientProfile updatedProfile) {
        try {
            PatientProfile profile = patientService.updatePatientProfile(authentication.getName(), updatedProfile);
            return ResponseEntity.ok(ApiResponse.ok("Patient profile updated successfully", profile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
