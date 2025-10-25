package com.project.hrms.service;

import com.project.shopapp.dtos.ProductDTO;
import com.project.shopapp.dtos.ProductImageDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.exceptions.InvalidParamException;
import com.project.shopapp.models.Category;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.repositories.CategoryRepository;
import com.project.shopapp.repositories.ProductImageRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.responses.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;

    @Override
    public Product createProduct(ProductDTO productDTO) {
        Category existingCategory = categoryRepository
                .findById(productDTO.getCategoryId())
                .orElseThrow(() ->
                        new DataNotFoundException(
                                "Cannot find category with id: " + productDTO.getCategoryId()));
        Product newProduct = Product.builder()
                .name(productDTO.getName())
                .price(productDTO.getPrice())
                .description(productDTO.getDescription())
                .thumbnail(productDTO.getThumbnail())
                .category(existingCategory)
                .build();
        return productRepository.save(newProduct);
    }

    @Override
    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new DataNotFoundException("Cannot find product with id: " + productId));
    }

    @Override
    public Page<ProductResponse> getAllProducts(PageRequest pageRequest) {
        //vi productResponse ko la class tra ve cho client nen ko co findall. nen la dung product roi anh xa tu product sang productResponse
        return productRepository.findAll(pageRequest).map(ProductResponse::fromProduct);
    }

    @Override
    public List<Product> getProductsByCategoryId(Long productId) {
        return productRepository.findByCategory_Id(productId);
    }

    @Override
    public Product updateProduct(Long productId, ProductDTO productDTO) {
        Product existingProduct = getProductById(productId);
        if (existingProduct != null) {
            Category existingCategory = categoryRepository
                    .findById(productDTO.getCategoryId())
                    .orElseThrow(() ->
                            new DataNotFoundException(
                                    "Cannot find category with id: " + productDTO.getCategoryId()));

            existingProduct.setName(productDTO.getName());
            existingProduct.setCategory(existingCategory);
            existingProduct.setPrice(productDTO.getPrice());
            existingProduct.setDescription(productDTO.getDescription());
            existingProduct.setThumbnail(productDTO.getThumbnail());
            return productRepository.save(existingProduct);
        }
        //ten file ko lưu trong model ma luu ten file trong bang product image
        return null;
    }

    @Override
    public void deleteProduct(Long productId) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        optionalProduct.ifPresent(productRepository::delete);
    }

    @Override
    public boolean existsProductByName(String name) {
        return productRepository.existsByName(name);
    }

    @Override
    public ProductImage createProductImage(Long productId, ProductImageDTO productImageDTO) {
        Product existingProduct = getProductById(productId);
        //neu > 5 thi khong tao productImage
        int size = productImageRepository.findByProductId(productId).size();
        if (size >= ProductImage.MAXIMUM_IMAGES_PER_PRODUCT) {
            throw new InvalidParamException("Number of images must be <= " + ProductImage.MAXIMUM_IMAGES_PER_PRODUCT);
        }

        ProductImage productImage = ProductImage
                .builder()
                .product(existingProduct)
                .imageUrl(productImageDTO.getImageUrl())
                .build();
        // khong cho insert qua 5 anh cho 1 san pham

        return productImageRepository.save(productImage);
    }
}
