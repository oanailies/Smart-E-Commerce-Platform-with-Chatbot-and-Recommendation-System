package product_service.domain.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import product_service.domain.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import product_service.domain.specification.ProductSpecification;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository
        extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    List<Product> findByCategory(Category category);
    List<Product> findByGender(Gender gender);
    List<Product> findByBrand(Brand brand);

    boolean existsByNameAndBrandIdAndSizeIdAndColorIdAndShadeId(
            String name, Long brandId, Long sizeId, Long colorId, Long shadeId);

    boolean existsByNameAndBrandIdNot(String name, Long brandId);

    List<Product> findBySize(Size size);

    List<Product> findByNameAndBrandId(String name, Long brandId);


    @Query("SELECT p FROM Product p " +
            "LEFT JOIN p.category c " +
            "LEFT JOIN p.brand b " +
            "LEFT JOIN p.shade s " +
            "WHERE LOWER(p.name) LIKE %:keyword% " +
            "OR LOWER(p.description) LIKE %:keyword% " +
            "OR LOWER(c.name) LIKE %:keyword% " +
            "OR LOWER(b.name) LIKE %:keyword% " +
            "OR LOWER(s.name) LIKE %:keyword%")
    List<Product> searchByKeyword(@Param("keyword") String keyword);


    List<Product> findByOnSaleTrue();

    @Query("SELECT p FROM Product p " +
            "WHERE (:brandId IS NULL OR p.brand.id = :brandId) " +
            "AND (:categoryIds IS NULL OR p.category.id IN :categoryIds)")
    List<Product> findByBrandAndCategoryIds(
            @Param("brandId") Long brandId,
            @Param("categoryIds") List<Long> categoryIds
    );



}


