package com.doctorapp.dto;

import com.doctorapp.entity.Appointment;
import com.doctorapp.entity.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AppointmentResponseDto {
    private Long id;
    private Long patientId;
    private String patientName;
    private String patientEmail;
    private String patientPhone;

    private Long doctorId;
    private Long doctorUserId;
    private String doctorName;
    private String doctorSpecialization;
    private String clinicAddress;
    private Double consultationFee;

    private LocalDate appointmentDate;
    private String startTime;
    private String endTime;
    private AppointmentStatus status;
    private String reason;
    private String prescription;
    private String doctorNotes;
    private LocalDateTime createdAt;

    public AppointmentResponseDto() {}

    public AppointmentResponseDto(Appointment apt) {
        if (apt != null) {
            this.id = apt.getId();
            if (apt.getPatient() != null) {
                this.patientId = apt.getPatient().getId();
                this.patientName = apt.getPatient().getFullName();
                this.patientEmail = apt.getPatient().getEmail();
                this.patientPhone = apt.getPatient().getPhone();
            }
            if (apt.getDoctor() != null) {
                this.doctorId = apt.getDoctor().getId();
                if (apt.getDoctor().getUser() != null) {
                    this.doctorUserId = apt.getDoctor().getUser().getId();
                    this.doctorName = apt.getDoctor().getUser().getFullName();
                }
                this.doctorSpecialization = apt.getDoctor().getSpecialization();
                this.clinicAddress = apt.getDoctor().getClinicAddress();
                this.consultationFee = apt.getDoctor().getConsultationFee();
            }
            this.appointmentDate = apt.getAppointmentDate();
            this.startTime = apt.getStartTime();
            this.endTime = apt.getEndTime();
            this.status = apt.getStatus();
            this.reason = apt.getReason();
            this.prescription = apt.getPrescription();
            this.doctorNotes = apt.getDoctorNotes();
            this.createdAt = apt.getCreatedAt();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public Long getDoctorUserId() {
        return doctorUserId;
    }

    public void setDoctorUserId(Long doctorUserId) {
        this.doctorUserId = doctorUserId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorSpecialization() {
        return doctorSpecialization;
    }

    public void setDoctorSpecialization(String doctorSpecialization) {
        this.doctorSpecialization = doctorSpecialization;
    }

    public String getClinicAddress() {
        return clinicAddress;
    }

    public void setClinicAddress(String clinicAddress) {
        this.clinicAddress = clinicAddress;
    }

    public Double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(Double consultationFee) {
        this.consultationFee = consultationFee;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }

    public String getDoctorNotes() {
        return doctorNotes;
    }

    public void setDoctorNotes(String doctorNotes) {
        this.doctorNotes = doctorNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
