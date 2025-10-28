package product_service.application.service;

import product_service.domain.model.Brand;
import product_service.domain.repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BrandService {

    @Autowired
    private BrandRepository brandRepository;

    public Brand createBrand(String name) {
        if (brandRepository.findByName(name) != null) {
            throw new RuntimeException("Brand already exists");
        }
        return brandRepository.save(new Brand(name));
    }

    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    public Brand getBrandById(Long id) {
        return brandRepository.findById(id).orElse(null);
    }
}
