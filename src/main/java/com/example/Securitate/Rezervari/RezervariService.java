package com.example.Securitate.Rezervari;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RezervariService {

    @Autowired
    private RezervariRepository rezervariRepository;

    public Rezervari createRezervare(Rezervari rezervare) {
        return rezervariRepository.save(rezervare);
    }

    public List<Rezervari> getAllRezervari() {
        return rezervariRepository.findAll();
    }

    public Rezervari getRezervareById(Long id) {
        return rezervariRepository.findById(id).orElse(null);
    }

    public void deleteRezervare(Long id) {
        rezervariRepository.deleteById(id);
    }
}

