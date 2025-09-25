package com.example.Securitate.reservation;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationQueryService reservationQueryService;

    public ReservationController(ReservationService reservationService,
                                 ReservationQueryService reservationQueryService) {
        this.reservationService = reservationService;
        this.reservationQueryService = reservationQueryService;
    }

    /** Creează o rezervare (necesită autentificare prin securitatea globală). */
    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationRequest request) {
        return new ResponseEntity<>(reservationService.create(request), HttpStatus.CREATED);
    }

    /** Rezervările userului curent (istoric). */
    @GetMapping("/my")
    public List<ReservationResponse> my() {
        return reservationService.myReservations();
    }

    /** Anulează o rezervare (owner sau admin). */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        reservationService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    /** (ADMIN) Toate rezervările (ordonate după start desc în service). */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ReservationResponse> all() {
        return reservationService.all();
    }

    /** (ADMIN) Update parțial rezervare (dată/ora/status) cu verificare de suprapuneri. */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservationResponse> adminUpdate(
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequest req
    ) {
        req.setId(id);
        return ResponseEntity.ok(reservationService.adminUpdate(req));
    }

    /** (ADMIN) Ștergere rezervare. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminDelete(@PathVariable Long id) {
        reservationService.adminDelete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Intervalele indisponibile pentru o mașină într-o fereastră [from, to] (inclusiv).
     * Folosit de frontend pentru a bloca zilele/orele în calendar.
     *
     * Exemplu:
     *   GET /api/v1/reservations/unavailable?carId=1&from=2025-09-01&to=2025-10-31
     */
    @GetMapping("/unavailable")
    public List<UnavailableRange> unavailable(
            @RequestParam Long carId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return reservationQueryService.unavailable(carId, from, to);
    }
}
