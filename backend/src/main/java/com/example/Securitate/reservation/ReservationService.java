package com.example.Securitate.reservation;

import com.example.Securitate.Cars.Car;
import com.example.Securitate.Cars.CarRepository;
import com.example.Securitate.User.User;
import com.example.Securitate.User.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            CarRepository carRepository,
            UserRepository userRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.carRepository = carRepository;
        this.userRepository = userRepository;
    }


    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));
    }


    @Transactional
    public ReservationResponse create(ReservationRequest req) {
        if (req.getStartAt() == null || req.getEndAt() == null) {
            throw new IllegalArgumentException("Start/end nu pot fi nule");
        }
        if (!req.getEndAt().isAfter(req.getStartAt())) {
            throw new IllegalArgumentException("End trebuie să fie după start");
        }
        if (req.getStartAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start trebuie să fie în viitor");
        }

        Car car = carRepository.findById(req.getCarId())
                .orElseThrow(() -> new IllegalArgumentException("Mașina nu există"));
        User user = currentUser();

        boolean overlap = reservationRepository.existsOverlap(
                car.getId(), req.getStartAt(), req.getEndAt()
        );
        if (overlap) {
            throw new IllegalStateException("Mașina nu este disponibilă în intervalul selectat");
        }

        Reservation r = new Reservation();
        r.setUser(user);
        r.setCar(car);
        r.setStartAt(req.getStartAt());
        r.setEndAt(req.getEndAt());
        r.setStatus(ReservationStatus.CONFIRMED); // sau HOLD, după fluxul tău

        Reservation saved = reservationRepository.save(r);

        String carLabel = car.getBrand() + " " + car.getModel() + " " + car.getYear();
        return new ReservationResponse(
                saved.getId(),
                car.getId(),
                carLabel,
                saved.getStartAt(),
                saved.getEndAt(),
                saved.getStatus()
        );
    }


    @Transactional(readOnly = true)
    public List<ReservationResponse> myReservations() {
        Long userId = Long.valueOf(currentUser().getId());
        return reservationRepository.findByUserIdOrderByStartAtDesc(userId)
                .stream()
                .map(r -> new ReservationResponse(
                        r.getId(),
                        r.getCar().getId(),
                        r.getCar().getBrand() + " " + r.getCar().getModel() + " " + r.getCar().getYear(),
                        r.getStartAt(),
                        r.getEndAt(),
                        r.getStatus()
                ))
                .toList();
    }


    @Transactional
    public void cancel(Long id) {
        Reservation r = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rezervarea nu există"));
        r.setStatus(ReservationStatus.CANCELLED); // asigură-te că enumul tău are CANCELLED
    }


    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<ReservationResponse> all() {
        return reservationRepository.findAllByOrderByStartAtDesc()
                .stream()
                .map(r -> new ReservationResponse(
                        r.getId(),
                        r.getCar().getId(),
                        r.getCar().getBrand() + " " + r.getCar().getModel() + " " + r.getCar().getYear(),
                        r.getStartAt(),
                        r.getEndAt(),
                        r.getStatus()
                ))
                .toList();
    }


    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ReservationResponse adminUpdate(ReservationUpdateRequest req) {
        var r = reservationRepository.findById(req.getId())
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        var newStart = (req.getStartAt() != null) ? req.getStartAt() : r.getStartAt();
        var newEnd   = (req.getEndAt()   != null) ? req.getEndAt()   : r.getEndAt();
        if (!newStart.isBefore(newEnd)) {
            throw new IllegalArgumentException("startAt must be before endAt");
        }

        boolean overlaps = reservationRepository
                .existsOverlappingForCarExcludingId(r.getCar().getId(), r.getId(), newStart, newEnd);
        if (overlaps) {
            throw new IllegalStateException("Interval overlaps another reservation for this car");
        }

        r.setStartAt(newStart);
        r.setEndAt(newEnd);
        if (req.getStatus() != null) r.setStatus(req.getStatus());
        var saved = reservationRepository.save(r);

        return new ReservationResponse(
                saved.getId(),
                saved.getCar().getId(),
                saved.getCar().getBrand() + " " + saved.getCar().getModel() + " " + saved.getCar().getYear(),
                saved.getStartAt(),
                saved.getEndAt(),
                saved.getStatus()
        );
    }


    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void adminDelete(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new IllegalArgumentException("Reservation not found");
        }
        reservationRepository.deleteById(id);
    }
}
