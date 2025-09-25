package com.example.Securitate.reservation;

import java.time.LocalDateTime;

public class UnavailableRange {
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    public UnavailableRange(LocalDateTime startAt, LocalDateTime endAt) {
        this.startAt = startAt;
        this.endAt = endAt;
    }
    public LocalDateTime getStartAt() { return startAt; }
    public LocalDateTime getEndAt() { return endAt; }
}
