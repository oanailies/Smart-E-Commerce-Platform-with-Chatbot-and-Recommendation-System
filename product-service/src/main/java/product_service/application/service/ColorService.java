package product_service.application.service;

import product_service.domain.model.Color;
import product_service.domain.repository.ColorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ColorService {

    @Autowired
    private ColorRepository colorRepository;

    public Color createColor(String name, String imageUrl) {
        return colorRepository.save(new Color(name, imageUrl));
    }

    public List<Color> getAllColors() {
        return colorRepository.findAll();
    }

    public Optional<Color> getColorById(Long id) {
        return colorRepository.findById(id);
    }

    public Optional<Color> updateColor(Long id, String name, String imageUrl) {
        return colorRepository.findById(id).map(color -> {
            color.setName(name);
            color.setImageUrl(imageUrl);
            return colorRepository.save(color);
        });
    }

    public void deleteColor(Long id) {
        colorRepository.deleteById(id);
    }
}
