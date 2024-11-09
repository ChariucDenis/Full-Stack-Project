package com.example.Securitate.Rezervari;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/v1/rezervari")
public class RezervariController {

    @Autowired
    private RezervariService rezervariService;

    @PostMapping
    public ResponseEntity<Rezervari> createRezervare(@RequestBody Rezervari rezervare) {
        Rezervari createdRezervare = rezervariService.createRezervare(rezervare);
        return ResponseEntity.ok(createdRezervare);
    }

    @GetMapping
    public ResponseEntity<List<Rezervari>> getAllRezervari() {
        List<Rezervari> rezervari = rezervariService.getAllRezervari();
        return ResponseEntity.ok(rezervari);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rezervari> getRezervareById(@PathVariable Long id) {
        Rezervari rezervare = rezervariService.getRezervareById(id);
        if (rezervare == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(rezervare);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRezervare(@PathVariable Long id) {
        rezervariService.deleteRezervare(id);
        return ResponseEntity.noContent().build();
    }
}

