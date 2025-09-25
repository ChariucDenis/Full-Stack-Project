package com.example.Securitate.reservation;

public enum ReservationStatus {
    HOLD, // optional – dacă vei avea plăți
    CONFIRMED,
    CANCELLED,
    COMPLETED,
    EXPIRED // optional – expiră hold-ul
}
