package com.example.Securitate.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Verifică dacă există o rezervare care se suprapune pentru aceeași mașină
     * în intervalul [startAt, endAt) și care are status activ (HOLD/CONFIRMED).
     *
     * Suprapunerea e definită ca: (existing.start < new.end) AND (existing.end > new.start)
     */
    @Query("""
        SELECT COUNT(r) > 0
        FROM Reservation r
        WHERE r.car.id = :carId
          AND r.status IN (
              com.example.Securitate.reservation.ReservationStatus.HOLD,
              com.example.Securitate.reservation.ReservationStatus.CONFIRMED
          )
          AND (r.startAt < :endAt AND r.endAt > :startAt)
    """)
    boolean existsOverlap(@Param("carId") Long carId,
                          @Param("startAt") LocalDateTime startAt,
                          @Param("endAt") LocalDateTime endAt);

    /**
     * Rezervările unui user, ordonate descrescător după startAt (pentru istoric).
     */
    List<Reservation> findByUserIdOrderByStartAtDesc(Long userId);

    /**
     * Intervalele indisponibile pentru o mașină într-o fereastră [fromStart, toEnd).
     * Folosit pentru a „gri” zilele ocupate în date-picker pe frontend.
     */
    @Query("""
        SELECT r
        FROM Reservation r
        WHERE r.car.id = :carId
          AND r.status IN (
              com.example.Securitate.reservation.ReservationStatus.HOLD,
              com.example.Securitate.reservation.ReservationStatus.CONFIRMED
          )
          AND (r.startAt < :toEnd AND r.endAt > :fromStart)
        ORDER BY r.startAt ASC
    """)
    List<Reservation> findUnavailableForWindow(@Param("carId") Long carId,
                                               @Param("fromStart") LocalDateTime fromStart,
                                               @Param("toEnd") LocalDateTime toEnd);
    List<Reservation> findAllByOrderByStartAtDesc();

    @Query("""
  SELECT CASE WHEN COUNT(r)>0 THEN TRUE ELSE FALSE END
  FROM Reservation r
  WHERE r.car.id = :carId
    AND r.id <> :excludeId
    AND (r.startAt < :endAt AND r.endAt > :startAt)
    AND r.status IN (
      com.example.Securitate.reservation.ReservationStatus.HOLD,
      com.example.Securitate.reservation.ReservationStatus.CONFIRMED
    )
""")
    boolean existsOverlappingForCarExcludingId(@Param("carId") Long carId,
                                               @Param("excludeId") Long excludeId,
                                               @Param("startAt") java.time.LocalDateTime startAt,
                                               @Param("endAt") java.time.LocalDateTime endAt);

}