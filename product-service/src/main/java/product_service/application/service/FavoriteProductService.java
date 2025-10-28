package product_service.application.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import product_service.domain.model.FavoriteProduct;
import product_service.domain.model.Product;
import product_service.domain.repository.FavoriteProductRepository;
import product_service.domain.repository.ProductRepository;

import java.util.List;

@Service
public class FavoriteProductService {

    @Autowired
    private FavoriteProductRepository favoriteProductRepository;

    @Autowired
    private ProductRepository productRepository;

    public FavoriteProduct addFavorite(Long clientId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        FavoriteProduct favorite = new FavoriteProduct(clientId, product);
        return favoriteProductRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(Long clientId, Long productId) {
        favoriteProductRepository.deleteByClientIdAndProductId(clientId, productId);
    }

    public List<FavoriteProduct> getFavoritesByClient(Long clientId) {
        return favoriteProductRepository.findByClientId(clientId);
    }

    public boolean isFavorite(Long clientId, Long productId) {
        return favoriteProductRepository.findByClientIdAndProductId(clientId, productId).isPresent();
    }
}
