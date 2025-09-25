package com.example.Securitate.reservation;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ReservationUpdateRequest {
    @NotNull
    private Long id;

    // opționale: dacă nu le trimiți, rămân la fel
    @Future
    private LocalDateTime startAt;

    @Future
    private LocalDateTime endAt;

    private ReservationStatus status; // ex: PENDING, CONFIRMED, CANCELED

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
}
