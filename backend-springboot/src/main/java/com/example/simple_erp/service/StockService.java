package com.example.simple_erp.service;

import com.example.simple_erp.dto.StockInRequest;
import com.example.simple_erp.entity.Product;
import com.example.simple_erp.entity.StockIn;
import com.example.simple_erp.repository.ProductRepository;
import com.example.simple_erp.repository.StockInRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class StockService {

    private final ProductRepository productRepository;
    private final StockInRepository stockInRepository;

    public StockService(ProductRepository productRepository, StockInRepository stockInRepository) {
        this.productRepository = productRepository;
        this.stockInRepository = stockInRepository;
    }

    @Transactional
    public StockIn addStock(StockInRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setStock(product.getStock() + request.quantity());
        productRepository.save(product);

        StockIn stockIn = new StockIn(product, request.quantity());
        return stockInRepository.save(stockIn);
    }
}