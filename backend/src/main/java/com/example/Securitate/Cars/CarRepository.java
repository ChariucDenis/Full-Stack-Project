package com.example.Securitate.Cars;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {


    Optional<Car> findCarByModel(String model);
    Optional<Car> findCarById(Long CarId);
    // Cea mai ieftină mașină (LIMIT 1)
    @Query(value = "SELECT * FROM car ORDER BY price_per_day ASC LIMIT 1", nativeQuery = true)
    Optional<Car> findCheapest();

    // Cel mai apropiat <= preț (vecin inferior sau exact)
    @Query(value = "SELECT * FROM car WHERE price_per_day <= :price "
            + "ORDER BY price_per_day DESC LIMIT 1", nativeQuery = true)
    Optional<Car> findNearestLowerOrEqual(@Param("price") Integer price);

    // Cel mai apropiat >= preț (vecin superior sau exact)
    @Query(value = "SELECT * FROM car WHERE price_per_day >= :price "
            + "ORDER BY price_per_day ASC LIMIT 1", nativeQuery = true)
    Optional<Car> findNearestHigherOrEqual(@Param("price") Integer price);
}