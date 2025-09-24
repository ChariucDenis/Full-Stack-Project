package com.example.Securitate.Cars;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CarService {

    private final CarRepository carRepository;

    @Autowired
    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public List<Car> getCars() {
        return carRepository.findAll();
    }

    public void addNewCar(Car car) {
        Optional<Car> carOptional = carRepository.findCarByModel(car.getModel());
        if (carOptional.isPresent()) {
            throw new IllegalStateException("Model taken");
        }
        carRepository.save(car);
    }

    public void deleteCar(Long carId) {
        boolean exists = carRepository.existsById(carId);
        if (!exists) {
            throw new IllegalStateException("Car with id " + carId + " does not exist");
        }
        carRepository.deleteById(carId);
    }

    @Transactional
    public void updateCar(Long carId, Integer price_per_day, String brand, String model, String color,
                          Integer year, String transmission, String fuel_type, Float emissions,
                          Float fuel_consumption, byte[] image, String imageName, String imageType) {

        Car car = carRepository.findCarById(carId).orElseThrow(() ->
                new IllegalStateException("Car with id " + carId + " does not exist"));

        if (price_per_day != null && price_per_day > 0 && !price_per_day.equals(car.getPrice_per_day())) {
            car.setPrice_per_day(price_per_day);
        }

        if (brand != null && brand.length() > 0 && !brand.equals(car.getBrand())) {
            car.setBrand(brand);
        }

        if (model != null && model.length() > 0 && !model.equals(car.getModel())) {
            car.setModel(model);
        }

        if (color != null && color.length() > 0 && !color.equals(car.getColor())) {
            car.setColor(color);
        }

        if (year != null && year > 0 && !year.equals(car.getYear())) {
            car.setYear(year);
        }

        if (transmission != null && transmission.length() > 0 && !transmission.equals(car.getTransmission())) {
            car.setTransmission(transmission);
        }

        if (fuel_type != null && fuel_type.length() > 0 && !fuel_type.equals(car.getFuel_type())) {
            car.setFuel_type(fuel_type);
        }

        if (emissions != null && emissions > 0) {
            car.setCO2_emissions(emissions);
        }

        if (fuel_consumption != null && fuel_consumption > 0) {
            car.setFuel_consumption(fuel_consumption);
        }

        if (image != null) {
            car.setImage(image);
            car.setImageName(imageName);
            car.setImageType(imageType);
        }

        carRepository.save(car);
    }

    public Optional<Car> getCar(Long id) {
        return carRepository.findById(id);
    }
}
