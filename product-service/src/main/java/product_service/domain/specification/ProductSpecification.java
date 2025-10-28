package product_service.domain.specification;

import org.springframework.data.jpa.domain.Specification;
import product_service.domain.model.*;

import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification implements Specification<Product> {

    private final Long brandId;
    private final Long categoryId;
    private final Long colorId;
    private final Long sizeId;
    private final Long shadeId;
    private final Gender gender;

    public ProductSpecification(Long brandId, Long categoryId, Long colorId,
                                Long sizeId, Long shadeId, Gender gender) {
        this.brandId = brandId;
        this.categoryId = categoryId;
        this.colorId = colorId;
        this.sizeId = sizeId;
        this.shadeId = shadeId;
        this.gender = gender;
    }

    @Override
    public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {

        List<Predicate> predicates = new ArrayList<>();

        if (brandId != null) {
            predicates.add(cb.equal(root.get("brand").get("id"), brandId));
        }

        if (categoryId != null) {
            predicates.add(cb.equal(root.get("category").get("id"), categoryId));
        }

        if (colorId != null) {
            predicates.add(cb.equal(root.get("color").get("id"), colorId));
        }

        if (sizeId != null) {
            predicates.add(cb.equal(root.get("size").get("id"), sizeId));
        }

        if (shadeId != null) {
            predicates.add(cb.equal(root.get("shade").get("id"), shadeId));
        }

        if (gender != null) {
            predicates.add(cb.equal(root.get("gender"), gender));
        }


        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
