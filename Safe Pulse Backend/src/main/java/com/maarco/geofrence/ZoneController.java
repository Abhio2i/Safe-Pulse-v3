package com.maarco.geofrence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/zones")
public class ZoneController {

    @Autowired
    private SafeZoneRepository zoneRepository;

    @PostMapping
    public ResponseEntity<SafeZone> createZone(@RequestBody SafeZone zone) {
        zone.setId(null); // Ensure new ID is generated
        return ResponseEntity.ok(zoneRepository.save(zone));
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<List<SafeZone>> getUserZones(@PathVariable String email) {
        return ResponseEntity.ok(zoneRepository.findByCreatedByOrSharedWithContaining(email, email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteZone(@PathVariable String id, @RequestParam String email) {
        Optional<SafeZone> zoneOptional = zoneRepository.findById(id);
        if (zoneOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Zone not found with ID: " + id);
        }

        SafeZone zone = zoneOptional.get();
        if (!zone.getCreatedBy().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the creator can delete this zone");
        }

        zoneRepository.deleteById(id);
        return ResponseEntity.ok("Zone deleted successfully");
    }
}