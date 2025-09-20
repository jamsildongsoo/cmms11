package com.cmms11.web;

import com.cmms11.plant.Plant;
import com.cmms11.plant.PlantService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plants")
public class PlantController {
    private final PlantService service;

    public PlantController(PlantService service) {
        this.service = service;
    }

    @GetMapping
    public Page<Plant> list(@RequestParam(name = "q", required = false) String q, Pageable pageable) {
        return service.list(q, pageable);
    }

    @GetMapping("/{plantId}")
    public ResponseEntity<Plant> get(@PathVariable String plantId) {
        return ResponseEntity.ok(service.get(plantId));
    }

    @PostMapping
    public ResponseEntity<Plant> create(@Valid @RequestBody Plant plant) {
        Plant saved = service.create(plant);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{plantId}")
    public ResponseEntity<Plant> update(@PathVariable String plantId, @Valid @RequestBody Plant plant) {
        Plant updated = service.update(plantId, plant);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{plantId}")
    public ResponseEntity<Void> delete(@PathVariable String plantId) {
        service.delete(plantId);
        return ResponseEntity.noContent().build();
    }
}
