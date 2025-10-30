package com.example.Securitate.ai;

import com.example.Securitate.Cars.Car;
import com.example.Securitate.Cars.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class CarTools {

    private final CarRepository carRepository;


    private LocalDateTime parseStart(String s) {
        try {
            return LocalDateTime.parse(s);
        } catch (DateTimeParseException ex) {
            return LocalDate.parse(s).atTime(10, 0);
        }
    }

    private LocalDateTime parseEnd(String s) {
        try {
            return LocalDateTime.parse(s);
        } catch (DateTimeParseException ex) {
            return LocalDate.parse(s).atTime(10, 0);
        }
    }

    // --- TOOL 1: Listare mașini ---
    @Tool(
            name = "listAllCars",
            description = "Returnează toate mașinile disponibile în baza de date."
    )
    public List<Car> listAllCars() {
        return carRepository.findAll();
    }


    @Tool(
            name = "findCarByModel",
            description = "Caută o mașină după model (ex: 'Golf', 'BMW X5') și returnează detaliile acesteia."
    )
    public Optional<Car> findCarByModel(String model) {
        return carRepository.findCarByModel(model);
    }

    // --- TOOL 3: Caută mașină după ID ---
    @Tool(
            name = "findCarById",
            description = "Caută o mașină după ID și returnează informațiile ei complete."
    )
    public Optional<Car> findCarById(Long id) {
        return carRepository.findCarById(id);
    }

    // --- TOOL 4: Calculează ofertă de preț ---
    public record Quote(long days, double subtotal, double deposit, double total) {}

    @Tool(
            name = "priceQuote",
            description = "Calculează prețul total pentru o mașină (carId) între start și end. Format dată: 'YYYY-MM-DD'."
    )
    public Quote priceQuote(Long carId, String start, String end) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("Car not found: " + carId));

        LocalDateTime s = parseStart(start);
        LocalDateTime e = parseEnd(end);

        long days = Math.max(1, Duration.between(s, e).toDays());



        double pricePerDay = car.getPrice_per_day();
        double subtotal = days * pricePerDay;
        double deposit = subtotal * 0.20;
        double total = subtotal + deposit;

        return new Quote(days, subtotal, deposit, total);
    }
    @Tool(
            name = "searchCars",
            description = """
    Caută mașini după filtre opționale: fuelType (diesel|benzina|hybrid|electric),
    maxPrice/zi (EUR), transmission (automat|manual) și interval (start/end, YYYY-MM-DD).
    Dacă e dat intervalul, întoarce DOAR mașinile disponibile.
  """
    )
    public List<Car> searchCars(String fuelType, Integer maxPrice, String transmission, String start, String end) {
        var all = carRepository.findAll();
        LocalDateTime s = (start!=null && !start.isBlank()) ? LocalDate.parse(start).atTime(0,0) : null;
        LocalDateTime e = (end  !=null && !end.isBlank())   ? LocalDate.parse(end).atTime(23,59) : null;

        List<Car> out = new ArrayList<>();
        for (Car c : all) {
            if (fuelType != null && c.getFuel_type()!=null &&
                    !c.getFuel_type().toLowerCase().contains(fuelType.toLowerCase())) continue;
            if (transmission != null && c.getTransmission()!=null &&
                    !c.getTransmission().toLowerCase().contains(transmission.toLowerCase())) continue;
            if (maxPrice != null && c.getPrice_per_day()!=null &&
                    c.getPrice_per_day() > maxPrice) continue;

            if (s != null && e != null) {

            }
            out.add(c);
        }
        return out;
    }

}
