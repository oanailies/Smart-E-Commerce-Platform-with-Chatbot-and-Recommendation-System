package product_service.application.controller;

import product_service.application.service.ShadeService;
import product_service.domain.model.Shade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/shades")
public class ShadeController {

    @Autowired
    private ShadeService shadeService;

    @PostMapping
    public ResponseEntity<Shade> createShade(@RequestBody Shade shade) {
        return ResponseEntity.ok(shadeService.createShade(shade.getName(), shade.getImageUrl()));
    }

    @GetMapping
    public ResponseEntity<List<Shade>> getAllShades() {
        return ResponseEntity.ok(shadeService.getAllShades());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Shade> getShadeById(@PathVariable Long id) {
        Optional<Shade> shade = shadeService.getShadeById(id);
        return shade.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Shade> updateShade(@PathVariable Long id, @RequestBody Shade shade) {
        Optional<Shade> updatedShade = shadeService.updateShade(id, shade.getName(), shade.getImageUrl());
        return updatedShade.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShade(@PathVariable Long id) {
        shadeService.deleteShade(id);
        return ResponseEntity.noContent().build();
    }
}
