package product_service.application.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import product_service.application.service.ProductService;
import product_service.domain.model.*;
import product_service.domain.repository.ProductRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Map<String, Object> requestBody) {
        try {
            String name = (String) requestBody.get("name");
            String description = (String) requestBody.get("description");
            double price = ((Number) requestBody.get("price")).doubleValue();
            int stock = ((Number) requestBody.get("stock")).intValue();

            Long categoryId = requestBody.containsKey("category") && requestBody.get("category") != null
                    ? ((Number) ((Map<String, Object>) requestBody.get("category")).get("id")).longValue()
                    : null;

            Long brandId = requestBody.containsKey("brand") && requestBody.get("brand") != null
                    ? ((Number) ((Map<String, Object>) requestBody.get("brand")).get("id")).longValue()
                    : null;

            Long shadeId = requestBody.containsKey("shade") && requestBody.get("shade") != null
                    ? ((Number) ((Map<String, Object>) requestBody.get("shade")).get("id")).longValue()
                    : null;

            Long sizeId = requestBody.containsKey("size") && requestBody.get("size") != null
                    ? ((Number) ((Map<String, Object>) requestBody.get("size")).get("id")).longValue()
                    : null;

            Long colorId = requestBody.containsKey("color") && requestBody.get("color") != null
                    ? ((Number) ((Map<String, Object>) requestBody.get("color")).get("id")).longValue()
                    : null;

            Gender gender = Gender.valueOf((String) requestBody.getOrDefault("gender", "FEMININ"));

            if (categoryId == null || brandId == null) {
                return ResponseEntity.badRequest().body(null);
            }

            Product createdProduct = productService.createProduct(
                    name, description, price, stock,
                    categoryId, brandId, gender,
                    sizeId, colorId, shadeId
            );

            return ResponseEntity.ok(createdProduct);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody
    ) {
        try {
            String name = (String) requestBody.get("name");
            String description = (String) requestBody.get("description");
            double price = ((Number) requestBody.get("price")).doubleValue();
            int stock = ((Number) requestBody.get("stock")).intValue();

            Long categoryId = requestBody.containsKey("categoryId") ? ((Number) requestBody.get("categoryId")).longValue() : null;
            Long brandId = requestBody.containsKey("brandId") ? ((Number) requestBody.get("brandId")).longValue() : null;
            Long shadeId = requestBody.get("shadeId") != null ? ((Number) requestBody.get("shadeId")).longValue() : null;

            if (categoryId == null || brandId == null) {
                return ResponseEntity.badRequest().body(null);
            }

            Product updated = productService.updateProduct(
                    id, name, description, price, stock, categoryId, brandId, shadeId
            );

            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }


    @GetMapping
    public ResponseEntity<List<Product>> getFilteredProducts(
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long colorId,
            @RequestParam(required = false) Long sizeId,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) String sort
    ) {
        List<Product> products = productService.getFilteredProducts(
                brandId, categoryId, colorId, sizeId, gender, sort
        );
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<?> addProductImages(@PathVariable Long id, @RequestBody List<String> imageUrls) {
        boolean success = productService.addImagesToProduct(id, imageUrls);
        if (!success) {
            return ResponseEntity.badRequest().body("Product not found!");
        }
        return ResponseEntity.ok("Images added successfully!");
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<List<ProductImage>> getProductImages(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductImages(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        boolean isDeleted = productService.deleteProductById(id);
        if (isDeleted) {
            return ResponseEntity.ok("Product deleted successfully.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
    }

    @GetMapping("/byCategory/{catId}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Long catId) {
        List<Product> products = productService.getProductsByCategory(catId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/byBrand/{brandId}")
    public ResponseEntity<List<Product>> getProductsByBrand(@PathVariable Long brandId) {
        List<Product> products = productService.getProductsByBrand(brandId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/sort")
    public ResponseEntity<List<Product>> sortProductsByPrice(@RequestParam String order) {
        List<Product> products = productService.sortProductsByPrice(order);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/bySize/{sizeId}")
    public ResponseEntity<List<Product>> getProductsBySize(@PathVariable Long sizeId) {
        List<Product> products = productService.getProductsBySize(sizeId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/byCategoryWithDesc/{catId}")
    public ResponseEntity<List<Product>> getProductsByCategoryWithDesc(@PathVariable Long catId) {
        List<Product> products = productService.getProductsByCategoryAndDescendants(catId);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}/update-name")
    public ResponseEntity<Product> updateProductName(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String newName = request.get("name");
        Optional<Product> optionalProduct = productRepository.findById(id);

        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setName(newName);
            productRepository.save(product);
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/variants")
    public ResponseEntity<List<Product>> getProductVariants(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        List<Product> variants = productService.getProductVariants(product.getName(), product.getBrand().getId());
        return ResponseEntity.ok(variants);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam(required = false) String q) {
        List<Product> results = productService.searchProducts(q);
        return ResponseEntity.ok(results);
    }


    @PutMapping("/{id}/apply-discount")
    public ResponseEntity<String> applyDiscount(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            double discount = ((Number) body.get("discountPercent")).doubleValue();
            boolean success = productService.applyDiscountToProduct(id, discount);
            if (success) {
                return ResponseEntity.ok("Discount applied successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to apply discount.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request body.");
        }
    }


    @PutMapping("/{id}/remove-discount")
    public ResponseEntity<String> removeDiscount(@PathVariable Long id) {
        boolean success = productService.removeDiscountFromProduct(id);
        if (success) {
            return ResponseEntity.ok("Discount removed successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to remove discount.");
        }
    }

    @GetMapping("/on-sale")
    public ResponseEntity<List<Product>> getProductsOnSale() {
        List<Product> products = productService.getProductsOnSale();
        return ResponseEntity.ok(products);
    }

    @PostMapping("/{id}/images/upload")
    public ResponseEntity<?> uploadImages(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files
    ) {
        boolean success = productService.saveUploadedImages(id, files);
        if (!success) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Product not found or failed to save images.");
        }
        return ResponseEntity.ok("Images uploaded successfully.");
    }


    @DeleteMapping("/{productId}/images/{imageId}")
    public ResponseEntity<String> deleteProductImage(@PathVariable Long productId, @PathVariable Long imageId) {
        boolean deleted = productService.deleteProductImage(productId, imageId);
        if (deleted) {
            return ResponseEntity.ok("Image deleted.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not found or doesn't belong to product.");
        }
    }

    @PutMapping("/{id}/decrease-stock")
    public ResponseEntity<String> decreaseStock(
            @PathVariable Long id,
            @RequestParam int quantity,
            @RequestHeader("Authorization") String token
    ) {
        boolean success = productService.decreaseStock(id, quantity, token);
        if (success) {
            return ResponseEntity.ok("Stock updated.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient stock.");
        }
    }



}
