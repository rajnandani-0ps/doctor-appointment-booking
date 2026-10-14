package com.doctorapp.dto;

import com.doctorapp.entity.DoctorProfile;

public class DoctorDto {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String specialization;
    private String qualification;
    private Integer experienceYears;
    private Double consultationFee;
    private String clinicAddress;
    private String bio;
    private String profileImageUrl;
    private Double rating;
    private Integer totalReviews;

    public DoctorDto() {}

    public DoctorDto(DoctorProfile profile) {
        if (profile != null) {
            this.id = profile.getId();
            if (profile.getUser() != null) {
                this.userId = profile.getUser().getId();
                this.fullName = profile.getUser().getFullName();
                this.email = profile.getUser().getEmail();
                this.phone = profile.getUser().getPhone();
            }
            this.specialization = profile.getSpecialization();
            this.qualification = profile.getQualification();
            this.experienceYears = profile.getExperienceYears();
            this.consultationFee = profile.getConsultationFee();
            this.clinicAddress = profile.getClinicAddress();
            this.bio = profile.getBio();
            this.profileImageUrl = profile.getProfileImageUrl();
            this.rating = profile.getRating();
            this.totalReviews = profile.getTotalReviews();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public Double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(Double consultationFee) {
        this.consultationFee = consultationFee;
    }

    public String getClinicAddress() {
        return clinicAddress;
    }

    public void setClinicAddress(String clinicAddress) {
        this.clinicAddress = clinicAddress;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Integer totalReviews) {
        this.totalReviews = totalReviews;
    }
}
