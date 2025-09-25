package com.example.Securitate.reservation;

import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDateTime;

public class ReservationRequest {
    @NotNull
    private Long carId;
    @NotNull
    private LocalDateTime startAt; // ISO-8601 din frontend
    @NotNull
    private LocalDateTime endAt;


    public Long getCarId() { return carId; }
    public void setCarId(Long carId) { this.carId = carId; }


    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }


    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
}
