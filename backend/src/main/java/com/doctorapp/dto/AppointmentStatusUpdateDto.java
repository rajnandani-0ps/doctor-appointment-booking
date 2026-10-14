package com.doctorapp.dto;

import com.doctorapp.entity.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public class AppointmentStatusUpdateDto {

    @NotNull(message = "Status is required")
    private AppointmentStatus status;

    private String doctorNotes;

    private String prescription;

    public AppointmentStatusUpdateDto() {}

    public AppointmentStatusUpdateDto(AppointmentStatus status, String doctorNotes, String prescription) {
        this.status = status;
        this.doctorNotes = doctorNotes;
        this.prescription = prescription;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getDoctorNotes() {
        return doctorNotes;
    }

    public void setDoctorNotes(String doctorNotes) {
        this.doctorNotes = doctorNotes;
    }

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }
}
