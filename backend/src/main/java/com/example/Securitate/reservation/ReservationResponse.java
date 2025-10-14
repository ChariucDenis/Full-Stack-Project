package com.example.Securitate.reservation;

import java.time.LocalDateTime;

public class ReservationResponse {
    private Long id;
    private Long carId;
    private String carLabel;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private ReservationStatus status;


    public ReservationResponse(Long id, Long carId, String carLabel, LocalDateTime startAt, LocalDateTime endAt, ReservationStatus status) {
        this.id = id; this.carId = carId; this.carLabel = carLabel; this.startAt = startAt; this.endAt = endAt; this.status = status;
    }



    public Long getId() { return id; }
    public Long getCarId() { return carId; }
    public String getCarLabel() { return carLabel; }
    public LocalDateTime getStartAt() { return startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public ReservationStatus getStatus() { return status; }
}
