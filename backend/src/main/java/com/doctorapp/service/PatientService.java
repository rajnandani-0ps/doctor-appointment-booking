package com.doctorapp.service;

import com.doctorapp.entity.PatientProfile;
import com.doctorapp.entity.User;
import com.doctorapp.repository.PatientProfileRepository;
import com.doctorapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {

    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;

    public PatientService(PatientProfileRepository patientProfileRepository, UserRepository userRepository) {
        this.patientProfileRepository = patientProfileRepository;
        this.userRepository = userRepository;
    }

    public PatientProfile getPatientProfile(String email) {
        return patientProfileRepository.findByUserEmail(email)
                .orElseGet(() -> {
                    User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
                    PatientProfile profile = new PatientProfile(user, null, null, null, null, null, null);
                    return patientProfileRepository.save(profile);
                });
    }

    @Transactional
    public PatientProfile updatePatientProfile(String email, PatientProfile updatedProfile) {
        PatientProfile existing = getPatientProfile(email);

        if (updatedProfile.getDateOfBirth() != null) existing.setDateOfBirth(updatedProfile.getDateOfBirth());
        if (updatedProfile.getGender() != null) existing.setGender(updatedProfile.getGender());
        if (updatedProfile.getBloodGroup() != null) existing.setBloodGroup(updatedProfile.getBloodGroup());
        if (updatedProfile.getAddress() != null) existing.setAddress(updatedProfile.getAddress());
        if (updatedProfile.getEmergencyContact() != null) existing.setEmergencyContact(updatedProfile.getEmergencyContact());
        if (updatedProfile.getMedicalHistory() != null) existing.setMedicalHistory(updatedProfile.getMedicalHistory());

        return patientProfileRepository.save(existing);
    }
}
