package com.doctorapp.service;

import com.doctorapp.dto.AuthRequest;
import com.doctorapp.dto.AuthResponse;
import com.doctorapp.dto.RegisterRequest;
import com.doctorapp.entity.DoctorProfile;
import com.doctorapp.entity.PatientProfile;
import com.doctorapp.entity.Role;
import com.doctorapp.entity.User;
import com.doctorapp.repository.DoctorProfileRepository;
import com.doctorapp.repository.PatientProfileRepository;
import com.doctorapp.repository.UserRepository;
import com.doctorapp.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       DoctorProfileRepository doctorProfileRepository,
                       PatientProfileRepository patientProfileRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        Long profileId = null;
        if (user.getRole() == Role.ROLE_DOCTOR) {
            profileId = doctorProfileRepository.findByUserId(user.getId())
                    .map(DoctorProfile::getId)
                    .orElse(null);
        } else if (user.getRole() == Role.ROLE_PATIENT) {
            profileId = patientProfileRepository.findByUserId(user.getId())
                    .map(PatientProfile::getId)
                    .orElse(null);
        }

        return new AuthResponse(
                jwt,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                profileId
        );
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName(),
                request.getPhone(),
                request.getRole()
        );
        user = userRepository.save(user);

        Long profileId = null;

        if (request.getRole() == Role.ROLE_DOCTOR) {
            String defaultAvatar = request.getProfileImageUrl() != null && !request.getProfileImageUrl().isBlank()
                    ? request.getProfileImageUrl()
                    : "https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=300&auto=format&fit=crop&q=80";

            DoctorProfile doctorProfile = new DoctorProfile(
                    user,
                    request.getSpecialization() != null ? request.getSpecialization() : "General Physician",
                    request.getQualification() != null ? request.getQualification() : "MBBS",
                    request.getExperienceYears() != null ? request.getExperienceYears() : 5,
                    request.getConsultationFee() != null ? request.getConsultationFee() : 50.0,
                    request.getClinicAddress() != null ? request.getClinicAddress() : "Main Healthcare Clinic",
                    request.getBio() != null ? request.getBio() : "Dedicated medical professional providing quality patient care.",
                    defaultAvatar
            );
            doctorProfile = doctorProfileRepository.save(doctorProfile);
            profileId = doctorProfile.getId();

        } else {
            PatientProfile patientProfile = new PatientProfile(
                    user,
                    request.getDateOfBirth(),
                    request.getGender(),
                    request.getBloodGroup(),
                    request.getAddress(),
                    request.getEmergencyContact(),
                    request.getMedicalHistory()
            );
            patientProfile = patientProfileRepository.save(patientProfile);
            profileId = patientProfile.getId();
        }

        String jwt = tokenProvider.generateTokenFromEmail(user.getEmail());

        return new AuthResponse(
                jwt,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                profileId
        );
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
}
