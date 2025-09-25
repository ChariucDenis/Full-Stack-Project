package com.example.Securitate.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationQueryService {

    private final ReservationRepository reservationRepository;

    public ReservationQueryService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    /**
     * Intervalele indisponibile pentru carId între zilele [from, to] (inclusiv).
     * Returnează perechi start/end (LocalDateTime) pe care le mapezi în UI.
     */
    @Transactional(readOnly = true)
    public List<UnavailableRange> unavailable(Long carId, LocalDate from, LocalDate to) {
        // includem "to" până la începutul zilei următoare (=> [from, to+1d) )
        LocalDateTime fromStart = from.atStartOfDay();
        LocalDateTime toEnd     = to.plusDays(1).atStartOfDay();

        return reservationRepository.findUnavailableForWindow(carId, fromStart, toEnd)
                .stream()
                .map(r -> new UnavailableRange(r.getStartAt(), r.getEndAt()))
                .toList();
    }
}
