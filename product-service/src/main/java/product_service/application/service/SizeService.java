package product_service.application.service;

import product_service.domain.model.Size;
import product_service.domain.repository.SizeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SizeService {

    @Autowired
    private SizeRepository sizeRepository;

    public Size createSize(String name) {
        return sizeRepository.save(new Size(name));
    }

    public List<Size> getAllSizes() {
        return sizeRepository.findAll();
    }

    public Optional<Size> getSizeById(Long id) {
        return sizeRepository.findById(id);
    }

    public Optional<Size> updateSize(Long id, String name) {
        return sizeRepository.findById(id).map(size -> {
            size.setName(name);
            return sizeRepository.save(size);
        });
    }

    public void deleteSize(Long id) {
        sizeRepository.deleteById(id);
    }
}
