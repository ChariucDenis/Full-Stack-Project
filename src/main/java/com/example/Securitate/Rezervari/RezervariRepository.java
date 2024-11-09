package com.example.Securitate.Rezervari;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RezervariRepository extends JpaRepository<Rezervari, Long> {
    // Additional query methods can be defined here if needed
}

