package com.example.Securitate.Cars;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    // SELECT * FROM student WHERE EMAIL LIKE .....
    Optional<Car> findCarByModel(String model);
    Optional<Car> findCarById(Long CarId);
}