package com.example.simple_erp.service;

import com.example.simple_erp.dto.CreateSalesOrderRequest;
import com.example.simple_erp.dto.UpdateSalesOrderStatusRequest;
import com.example.simple_erp.entity.Product;
import com.example.simple_erp.entity.SalesOrder;
import com.example.simple_erp.entity.SalesOrderItem;
import com.example.simple_erp.repository.ProductRepository;
import com.example.simple_erp.repository.SalesOrderItemRepository;
import com.example.simple_erp.repository.SalesOrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class SalesOrderService {

    private final ProductRepository productRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final DiscountRuleService discountRuleService;
    private final StockMovementService stockMovementService;

    public SalesOrderService(
            ProductRepository productRepository,
            SalesOrderRepository salesOrderRepository,
            SalesOrderItemRepository salesOrderItemRepository,
            DiscountRuleService discountRuleService,
            StockMovementService stockMovementService
    ) {
        this.productRepository = productRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderItemRepository = salesOrderItemRepository;
        this.discountRuleService = discountRuleService;
        this.stockMovementService = stockMovementService;
    }

    @Transactional
    public SalesOrder createSalesOrder(CreateSalesOrderRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStock() < request.quantity()) {
            throw new RuntimeException("Stock not enough");
        }

        BigDecimal price = product.getSellingPrice();
        BigDecimal quantity = BigDecimal.valueOf(request.quantity());

        BigDecimal subtotalBeforeDiscount = price.multiply(quantity);

        BigDecimal discountPercent = discountRuleService.getBestDiscountPercent(
                request.orderDate(),
                request.quantity()
        );

        BigDecimal discountAmount = subtotalBeforeDiscount
                .multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal totalAmount = subtotalBeforeDiscount.subtract(discountAmount);

        SalesOrder salesOrder = new SalesOrder(
                request.orderDate(),
                product.getId(),
                request.customerName(),
                request.phone(),
                request.address(),
                product.getName(),
                request.quantity(),
                request.shippingMethod(),
                request.trackingNumber(),
                request.shippingStatus(),
                request.paymentMethod(),
                request.paymentStatus(),
                "ACTIVE",
                "None",
                subtotalBeforeDiscount,
                discountPercent,
                discountAmount,
                totalAmount
        );

        SalesOrder savedOrder = salesOrderRepository.save(salesOrder);

        SalesOrderItem item = new SalesOrderItem(
                savedOrder,
                product,
                request.quantity(),
                price,
                totalAmount
        );

        salesOrderItemRepository.save(item);

        product.setStock(product.getStock() - request.quantity());
        productRepository.save(product);

        stockMovementService.createMovement(
                product,
                "OUT",
                request.quantity(),
                "SALES_ORDER",
                savedOrder.getId(),
                "Stock keluar karena sales order SO-" + String.format("%04d", savedOrder.getId())
        );

        return savedOrder;
    }

    public List<SalesOrder> getAllSalesOrders() {
        return salesOrderRepository.findAll();
    }

    public SalesOrder updateSalesOrderStatus(Long id, UpdateSalesOrderStatusRequest request) {
        SalesOrder salesOrder = salesOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sales order not found"));

        salesOrder.setOrderDate(request.orderDate());
        salesOrder.setTrackingNumber(request.trackingNumber());
        salesOrder.setShippingStatus(request.shippingStatus());
        salesOrder.setPaymentStatus(request.paymentStatus());
        salesOrder.setOrderStatus(request.orderStatus());
        salesOrder.setRefundStatus(request.refundStatus());

        return salesOrderRepository.save(salesOrder);
    }

    @Transactional
    public SalesOrder cancelSalesOrder(Long id) {
        SalesOrder salesOrder = salesOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sales order not found"));

        if (!"Preparing".equals(salesOrder.getShippingStatus())) {
            throw new RuntimeException("Order cannot be cancelled because it is already shipped");
        }

        if ("CANCELLED".equals(salesOrder.getOrderStatus())) {
            throw new RuntimeException("Order already cancelled");
        }

        Product product = productRepository.findById(salesOrder.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setStock(product.getStock() + salesOrder.getQuantity());
        productRepository.save(product);

        salesOrder.setOrderStatus("CANCELLED");
        salesOrder.setShippingStatus("Cancelled");

        stockMovementService.createMovement(
                product,
                "RETURN",
                salesOrder.getQuantity(),
                "SALES_ORDER",
                salesOrder.getId(),
                "Stock kembali karena sales order dibatalkan"
        );

        return salesOrderRepository.save(salesOrder);
    }

    public SalesOrder requestRefund(Long id) {
        SalesOrder salesOrder = salesOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sales order not found"));

        if ("Preparing".equals(salesOrder.getShippingStatus())) {
            throw new RuntimeException("Order is still preparing. Cancel the order instead");
        }

        salesOrder.setRefundStatus("Requested");

        return salesOrderRepository.save(salesOrder);
    }

    public SalesOrder trackOrder(Long orderId, String phone) {
        return salesOrderRepository.findByIdAndPhone(orderId, phone)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Transactional
    public void deleteSalesOrder(Long id) {
        salesOrderItemRepository.deleteItemsBySalesOrderId(id);
        salesOrderRepository.deleteById(id);
    }
}