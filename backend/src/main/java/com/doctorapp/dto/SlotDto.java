package com.doctorapp.dto;

import com.doctorapp.entity.AvailabilitySlot;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class SlotDto {
    private Long id;
    private Long doctorId;

    @NotNull(message = "Slot date is required")
    private LocalDate slotDate;

    @NotBlank(message = "Start time is required")
    private String startTime;

    @NotBlank(message = "End time is required")
    private String endTime;

    private boolean booked;

    public SlotDto() {}

    public SlotDto(AvailabilitySlot slot) {
        if (slot != null) {
            this.id = slot.getId();
            if (slot.getDoctor() != null) {
                this.doctorId = slot.getDoctor().getId();
            }
            this.slotDate = slot.getSlotDate();
            this.startTime = slot.getStartTime();
            this.endTime = slot.getEndTime();
            this.booked = slot.isBooked();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getSlotDate() {
        return slotDate;
    }

    public void setSlotDate(LocalDate slotDate) {
        this.slotDate = slotDate;
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

    public boolean isBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }
}
