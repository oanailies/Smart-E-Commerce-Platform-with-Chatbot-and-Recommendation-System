package product_service.application.service;

import product_service.domain.model.Shade;
import product_service.domain.repository.ShadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShadeService {

    @Autowired
    private ShadeRepository shadeRepository;

    public Shade createShade(String name, String imageUrl) {
        return shadeRepository.save(new Shade(name, imageUrl));
    }

    public List<Shade> getAllShades() {
        return shadeRepository.findAll();
    }

    public Optional<Shade> getShadeById(Long id) {
        return shadeRepository.findById(id);
    }

    public Optional<Shade> updateShade(Long id, String name, String imageUrl) {
        return shadeRepository.findById(id).map(shade -> {
            shade.setName(name);
            shade.setImageUrl(imageUrl);
            return shadeRepository.save(shade);
        });
    }

    public void deleteShade(Long id) {
        shadeRepository.deleteById(id);
    }
}
