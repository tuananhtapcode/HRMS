package com.project.hrms.service;

import com.project.shopapp.dtos.ProductDTO;
import com.project.shopapp.dtos.ProductImageDTO;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.responses.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IProductService {
    Product createProduct(ProductDTO productDTO);

    Product getProductById(Long productId);

    Page<ProductResponse> getAllProducts(PageRequest pageRequest);

    List<Product> getProductsByCategoryId(Long productId);

    Product updateProduct(Long id, ProductDTO productDTO);

    void deleteProduct(Long id);

    boolean existsProductByName(String name);

    ProductImage createProductImage(Long productId, ProductImageDTO productImageDTO);
}
