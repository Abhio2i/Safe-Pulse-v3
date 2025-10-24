//package com.maarco.geofrence;
//
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/safe-zones")
//public class SafeZoneController {
//
//    @Autowired
//    private SafeZoneRepository safeZoneRepository;
//
//    // Create a new safe zone
//    @PostMapping
//    public ResponseEntity<SafeZone> createSafeZone(@RequestBody SafeZone safeZone) {
//        SafeZone savedZone = safeZoneRepository.save(safeZone);
//        return ResponseEntity.ok(savedZone);
//    }
//
//    // Get all safe zones created by a specific user
//    @GetMapping("/created-by/{email}")
//    public ResponseEntity<List<SafeZone>> getZonesCreatedByUser(@PathVariable String email) {
//        List<SafeZone> zones = safeZoneRepository.findByCreatedBy(email);
//        return ResponseEntity.ok(zones);
//    }
//
//    // Get all safe zones shared with a specific user
//    @GetMapping("/shared-with/{email}")
//    public ResponseEntity<List<SafeZone>> getZonesSharedWithUser(@PathVariable String email) {
//        List<SafeZone> zones = safeZoneRepository.findBySharedWithContaining(email);
//        return ResponseEntity.ok(zones);
//    }
//
//    // Update a safe zone
//    @PutMapping("/{id}")
//    public ResponseEntity<SafeZone> updateSafeZone(@PathVariable String id, @RequestBody SafeZone updatedZone) {
//        return safeZoneRepository.findById(id)
//                .map(existingZone -> {
//                    existingZone.setName(updatedZone.getName());
//                    existingZone.setLatitude(updatedZone.getLatitude());
//                    existingZone.setLongitude(updatedZone.getLongitude());
//                    existingZone.setRadius(updatedZone.getRadius());
//                    existingZone.setType(updatedZone.getType());
//                    existingZone.setSharedWith(updatedZone.getSharedWith());
//                    existingZone.setSosSettings(updatedZone.getSosSettings());
//                    SafeZone savedZone = safeZoneRepository.save(existingZone);
//                    return ResponseEntity.ok(savedZone);
//                })
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    // Delete a safe zone
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteSafeZone(@PathVariable String id) {
//        safeZoneRepository.deleteById(id);
//        return ResponseEntity.noContent().build();
//    }
//}
