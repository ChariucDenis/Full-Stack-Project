package com.example.Securitate.Cars;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "https://bluerentcar.netlify.app")
@RestController
@RequestMapping(path = "api/v1/car")
public class CarController {

    private final CarService carService;

    @Autowired
    public CarController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping
    public List<Car> getCars() {
        return carService.getCars();
    }

    @PostMapping
    public ResponseEntity<String> registerNewCar(
            @RequestParam("brand") String brand,
            @RequestParam("model") String model,
            @RequestParam("year") Integer year,
            @RequestParam("color") String color,
            @RequestParam("transmission") String transmission,
            @RequestParam("fuel_type") String fuelType,
            @RequestParam("price_per_day") Integer pricePerDay,
            @RequestParam("emissions") float emissions,
            @RequestParam("fuel_consumption") float fuelConsumption,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        try {
            byte[] imageData = (image != null && !image.isEmpty()) ? image.getBytes() : null;
            String imageName = (image != null) ? image.getOriginalFilename() : null;
            String imageType = (image != null) ? image.getContentType() : null;

            Car car = new Car(brand, model, year, color, transmission, fuelType, pricePerDay,
                    emissions, fuelConsumption, imageData, imageName, imageType);

            carService.addNewCar(car);
            return ResponseEntity.ok("Car registered successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Car> getCarById(@PathVariable("id") Long id) {
        Optional<Car> car = carService.getCar(id);

        if (car.isPresent()) {
            return ResponseEntity.ok(car.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping(path = "{carId}")
    public ResponseEntity<String> deleteCar(@PathVariable("carId") Long carId) {
        carService.deleteCar(carId);
        return ResponseEntity.ok("Car deleted successfully!");
    }

    @PutMapping(path = "{carId}")
    public ResponseEntity<String> updateCar(
            @PathVariable("carId") Long carId,
            @RequestParam(required = false) Integer price_per_day,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String transmission,
            @RequestParam(required = false) String fuel_type,
            @RequestParam(required = false) Float emissions, // New field
            @RequestParam(required = false) Float fuel_consumption, // New field
            @RequestParam(value = "image", required = false) MultipartFile image) {

        try {
            byte[] imageData = null;
            String imageName = null;
            String imageType = null;

            if (image != null && !image.isEmpty()) {
                imageData = image.getBytes();
                imageName = image.getOriginalFilename();
                imageType = image.getContentType();
            }

            carService.updateCar(carId, price_per_day, brand, model, color, year, transmission,
                    fuel_type, emissions, fuel_consumption, imageData, imageName, imageType); // Modified method

            return ResponseEntity.ok("Car updated successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image");
        }
    }

    @GetMapping(path = "{carId}/image")
    public ResponseEntity<byte[]> getCarImage(@PathVariable("carId") Long carId) {
        Optional<Car> car = carService.getCar(carId);
        if (car.isPresent()) {
            byte[] image = car.get().getImage();
            String imageType = car.get().getImageType();

            if (image != null && imageType != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.set(HttpHeaders.CONTENT_TYPE, imageType);
                return new ResponseEntity<>(image, headers, HttpStatus.OK);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
