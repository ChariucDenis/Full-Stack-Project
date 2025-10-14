package com.example.Securitate.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {


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


    List<Reservation> findByUserIdOrderByStartAtDesc(Long userId);


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