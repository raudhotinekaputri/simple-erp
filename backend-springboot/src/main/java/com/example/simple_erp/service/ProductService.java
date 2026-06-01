package com.example.simple_erp.service;

import com.example.simple_erp.dto.CreateProductRequest;
import com.example.simple_erp.dto.UpdateProductRequest;
import com.example.simple_erp.entity.Product;
import com.example.simple_erp.repository.ProductRepository;
import com.example.simple_erp.repository.SalesOrderItemRepository;
import com.example.simple_erp.repository.StockInRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final StockInRepository stockInRepository;
    private final StockMovementService stockMovementService;

    public ProductService(
            ProductRepository productRepository,
            SalesOrderItemRepository salesOrderItemRepository,
            StockInRepository stockInRepository,
            StockMovementService stockMovementService
    ) {
        this.productRepository = productRepository;
        this.salesOrderItemRepository = salesOrderItemRepository;
        this.stockInRepository = stockInRepository;
        this.stockMovementService = stockMovementService;
    }

    public Product createProduct(CreateProductRequest request) {
        Product product = new Product(
                request.name(),
                request.stock(),
                request.minStock(),
                request.costPrice(),
                request.sellingPrice()
        );

        Product savedProduct = productRepository.save(product);

        if (request.stock() > 0) {
            stockMovementService.createMovement(
                    savedProduct,
                    "IN",
                    request.stock(),
                    "PRODUCT_INITIAL_STOCK",
                    savedProduct.getId(),
                    "Initial stock saat produk dibuat"
            );
        }

        return savedProduct;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setName(request.name());
        product.setStock(request.stock());
        product.setMinStock(request.minStock());
        product.setCostPrice(request.costPrice());
        product.setSellingPrice(request.sellingPrice());

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        salesOrderItemRepository.deleteItemsByProductId(id);
        stockInRepository.deleteByProductIdNative(id);

        productRepository.deleteById(id);
    }

    public Product addStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setStock(product.getStock() + quantity);

        Product savedProduct = productRepository.save(product);

        stockMovementService.createMovement(
                savedProduct,
                "IN",
                quantity,
                "STOCK_IN",
                savedProduct.getId(),
                "Stock masuk dari fitur Stock In"
        );

        return savedProduct;
    }
}