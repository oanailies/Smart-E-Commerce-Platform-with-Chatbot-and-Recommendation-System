package product_service.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import product_service.domain.model.*;
import product_service.domain.repository.*;
import product_service.infrastructure.client.EmailClient;
import product_service.infrastructure.client.UserClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SizeRepository sizeRepository;

    @Autowired
    private ColorRepository colorRepository;

    @Autowired
    private ShadeRepository shadeRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FavoriteProductRepository favoriteProductRepository;

    @Autowired
    private EmailClient emailClient;

    @Autowired
    private UserClient userClient;

    public Product createProduct(String name, String description, double price, int stock,
                                 Long categoryId, Long brandId, Gender gender,
                                 Long sizeId, Long colorId, Long shadeId) {

        boolean exists = productRepository.existsByNameAndBrandIdAndSizeIdAndColorIdAndShadeId(
                name, brandId, sizeId, colorId, shadeId);

        if (exists) {
            throw new RuntimeException("A product with the same name, brand, and attributes already exists!");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new RuntimeException("Brand not found"));

        Size size = (sizeId != null) ? sizeRepository.findById(sizeId).orElse(null) : null;
        Color color = (colorId != null) ? colorRepository.findById(colorId).orElse(null) : null;
        Shade shade = (shadeId != null) ? shadeRepository.findById(shadeId).orElse(null) : null;

        Product product = new Product(name, description, price, stock, category, brand, gender, size, color, shade);
        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public boolean deleteProductById(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean addImagesToProduct(Long productId, List<String> imageUrls) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isEmpty()) {
            return false;
        }

        Product product = optionalProduct.get();

        if (imageUrls == null || imageUrls.isEmpty()) {
            return true;
        }

        List<ProductImage> images = imageUrls.stream()
                .map(url -> new ProductImage(url, product))
                .toList();

        productImageRepository.saveAll(images);
        return true;
    }

    public List<ProductImage> getProductImages(Long productId) {
        return productImageRepository.findByProductId(productId);
    }

    public List<Product> getProductsByCategory(Long catId) {
        Category cat = categoryRepository.findById(catId).orElse(null);
        if (cat == null) return List.of();
        return productRepository.findByCategory(cat);
    }

    public List<Product> getProductsByBrand(Long brandId) {
        Brand brand = brandRepository.findById(brandId).orElse(null);
        if (brand == null) {
            return List.of();
        }
        return productRepository.findByBrand(brand);
    }

    public List<Product> sortProductsByPrice(String sortOrder) {
        Sort sort = Sort.unsorted();
        if ("asc".equalsIgnoreCase(sortOrder)) {
            sort = Sort.by("price").ascending();
        } else if ("desc".equalsIgnoreCase(sortOrder)) {
            sort = Sort.by("price").descending();
        }
        return productRepository.findAll(sort);
    }

    public List<Product> getProductsBySize(Long sizeId) {
        Size size = sizeRepository.findById(sizeId).orElse(null);
        if (size == null) {
            return List.of();
        }
        return productRepository.findBySize(size);
    }

    public List<Product> getFilteredProducts(
            Long brandId,
            Long categoryId,
            Long colorId,
            Long sizeId,
            Gender gender,
            String sortOrder
    ) {
        List<Product> filtered;
        List<Long> categoryIds = null;

        if (categoryId != null) {
            categoryIds = categoryService.getAllDescendantCategoryIds(categoryId);
            categoryIds.add(categoryId);
        }

        if (brandId != null || categoryIds != null) {
            filtered = productRepository.findByBrandAndCategoryIds(brandId, categoryIds);
        } else {
            filtered = productRepository.findAll();
        }

        Stream<Product> stream = filtered.stream();

        if (colorId != null) {
            stream = stream.filter(p -> p.getColor() != null && p.getColor().getId().equals(colorId));
        }

        if (sizeId != null) {
            stream = stream.filter(p -> p.getSize() != null && p.getSize().getId().equals(sizeId));
        }

        if (gender != null) {
            stream = stream.filter(p -> p.getGender() == gender);
        }

        if ("asc".equalsIgnoreCase(sortOrder)) {
            stream = stream.sorted(Comparator.comparing(Product::getPrice));
        } else if ("desc".equalsIgnoreCase(sortOrder)) {
            stream = stream.sorted(Comparator.comparing(Product::getPrice).reversed());
        }

        return stream.toList();
    }



    public List<Product> getProductsByCategoryAndDescendants(Long categoryId) {
        List<Long> allCatIds = categoryService.getAllDescendantCategoryIds(categoryId);
        List<Product> allProducts = productRepository.findAll();
        return allProducts.stream()
                .filter(p -> allCatIds.contains(p.getCategory().getId()))
                .toList();
    }

    public List<Product> getProductVariants(String productName, Long brandId) {
        return productRepository.findByNameAndBrandId(productName, brandId)
                .stream()
                .filter(p -> p.getShade() != null || p.getSize() != null)
                .collect(Collectors.toList());
    }


    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findAll();
        }
        return productRepository.searchByKeyword(keyword.toLowerCase());
    }


    public boolean applyDiscountToProduct(Long productId, double discountPercent) {
        Product product = getProductById(productId);
        if (product == null || discountPercent <= 0) return false;

        product.setOnSale(true);
        product.setDiscountPercent(discountPercent);

        double discountedPrice = product.getPrice() - (product.getPrice() * discountPercent / 100);
        product.setDiscountedPrice(discountedPrice);

        productRepository.save(product);
        return true;
    }


    public boolean removeDiscountFromProduct(Long productId) {
        Product product = getProductById(productId);
        if (product == null) return false;

        product.setOnSale(false);
        product.setDiscountPercent(0.0);
        product.setDiscountedPrice(null);

        productRepository.save(product);
        return true;
    }



    public boolean applyDiscountToCategory(Long categoryId, double discountPercent) {
        if (discountPercent <= 0) return false;
        List<Long> allCatIds = categoryService.getAllDescendantCategoryIds(categoryId);
        List<Product> toDiscount = productRepository.findAll().stream()
                .filter(p -> allCatIds.contains(p.getCategory().getId()))
                .collect(Collectors.toList());
        if (toDiscount.isEmpty()) return false;
        toDiscount.forEach(p -> {
            p.setOnSale(true);
            p.setDiscountPercent(discountPercent);
            p.setDiscountedPrice(p.getPrice() * (100 - discountPercent) / 100);
        });
        productRepository.saveAll(toDiscount);
        return true;
    }


    public boolean removeDiscountFromCategory(Long categoryId) {
        List<Long> allCatIds = categoryService.getAllDescendantCategoryIds(categoryId);
        List<Product> toRestore = productRepository.findAll().stream()
                .filter(p -> allCatIds.contains(p.getCategory().getId()))
                .collect(Collectors.toList());
        if (toRestore.isEmpty()) return false;
        toRestore.forEach(p -> {
            p.setOnSale(false);
            p.setDiscountPercent(0.0);
            p.setDiscountedPrice(null);
        });
        productRepository.saveAll(toRestore);
        return true;
    }

    public Product updateProduct(Long id, String name, String description, double price, int stock,
                                 Long categoryId, Long brandId, Long shadeId) {
        Product product = productRepository.findById(id).orElseThrow();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        product.setCategory(categoryRepository.findById(categoryId).orElse(null));
        product.setBrand(brandRepository.findById(brandId).orElse(null));
        if (shadeId != null) {
            product.setShade(shadeRepository.findById(shadeId).orElse(null));
        } else {
            product.setShade(null);
        }

        return productRepository.save(product);
    }


    public List<Product> getProductsOnSale() {
        return productRepository.findByOnSaleTrue();
    }

    public boolean updateProductImage(Long productId, Long imageId, String newImageUrl) {
        Optional<ProductImage> imageOpt = productImageRepository.findById(imageId);
        if (imageOpt.isPresent() && imageOpt.get().getProduct().getId().equals(productId)) {
            ProductImage image = imageOpt.get();
            image.setImageUrl(newImageUrl);
            productImageRepository.save(image);
            return true;
        }
        return false;
    }

    public boolean deleteProductImage(Long productId, Long imageId) {
        Optional<ProductImage> imageOpt = productImageRepository.findById(imageId);
        if (imageOpt.isPresent()) {
            ProductImage image = imageOpt.get();
            if (image.getProduct().getId().equals(productId)) {
                productImageRepository.deleteById(imageId);
                return true;
            }
        }
        return false;
    }


    public boolean saveUploadedImages(Long productId, List<MultipartFile> files) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isEmpty()) return false;

        Product product = optionalProduct.get();

        List<ProductImage> images = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            if (fileName == null || fileName.isBlank()) continue;
            String url = "http://localhost:5173/images/products/" + fileName;
            images.add(new ProductImage(url, product));
        }

        productImageRepository.saveAll(images);
        return true;
    }

    public boolean decreaseStock(Long productId, int quantity, String jwtToken) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || product.getStock() < quantity) {
            return false;
        }

        int oldStock = product.getStock();
        int newStock = oldStock - quantity;
        product.setStock(newStock);
        productRepository.save(product);
        if (newStock < oldStock && newStock <= 10 && newStock >= 1) {
            List<FavoriteProduct> favorites = favoriteProductRepository.findByProductId(productId);
            for (FavoriteProduct fav : favorites) {
                sendLowStockEmail(fav.getClientId(), product, newStock, jwtToken);
            }
        }

        return true;
    }

    private void sendLowStockEmail(Long clientId, Product product, int stock, String jwtToken) {
        String subject = "Hurry up! Low stock alert for your favorite product";
        String message = String.format(
                "Hello,\n\nThe product \"%s\" you have marked as favorite is now running low on stock (only %d items left).\n" +
                        "Hurry up and grab it before it sells out!\n\nWith love,\nMaison Belle Team 🌸",
                product.getName(), stock
        );

        emailClient.sendEmailToClient(clientId, jwtToken, subject, message);
    }


}
