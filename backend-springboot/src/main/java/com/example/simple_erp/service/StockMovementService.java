package com.example.simple_erp.service;

import com.example.simple_erp.entity.Product;
import com.example.simple_erp.entity.StockMovement;
import com.example.simple_erp.repository.StockMovementRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;

    public StockMovementService(StockMovementRepository stockMovementRepository) {
        this.stockMovementRepository = stockMovementRepository;
    }

    public StockMovement createMovement(
            Product product,
            String movementType,
            Integer quantity,
            String referenceType,
            Long referenceId,
            String note
    ) {
        StockMovement movement = new StockMovement(
                product.getId(),
                product.getName(),
                movementType,
                quantity,
                referenceType,
                referenceId,
                note
        );

        return stockMovementRepository.save(movement);
    }

    public List<StockMovement> getAllMovements() {
        return stockMovementRepository.findAllByOrderByCreatedAtDesc();
    }
}