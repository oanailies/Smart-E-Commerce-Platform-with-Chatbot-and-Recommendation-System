package product_service.application.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import product_service.application.service.FavoriteProductService;
import product_service.domain.model.FavoriteProduct;

import java.util.List;

@RestController
@RequestMapping("/favorites")
public class FavoriteProductController {

    @Autowired
    private FavoriteProductService favoriteProductService;

    @PostMapping("/add")
    public ResponseEntity<FavoriteProduct> addFavorite(@RequestParam Long clientId, @RequestParam Long productId) {
        return ResponseEntity.ok(favoriteProductService.addFavorite(clientId, productId));
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFavorite(@RequestParam Long clientId, @RequestParam Long productId) {
        favoriteProductService.removeFavorite(clientId, productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<FavoriteProduct>> getFavorites(@PathVariable Long clientId) {
        return ResponseEntity.ok(favoriteProductService.getFavoritesByClient(clientId));
    }

    @GetMapping("/is-favorite")
    public ResponseEntity<Boolean> isFavorite(@RequestParam Long clientId, @RequestParam Long productId) {
        boolean isFav = favoriteProductService.isFavorite(clientId, productId);
        return ResponseEntity.ok(isFav);
    }
}
