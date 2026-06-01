package com.example.simple_erp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_orders")
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate orderDate;

    private Long productId;

    private String customerName;
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String productName;
    private Integer quantity;

    private String shippingMethod;
    private String trackingNumber;
    private String shippingStatus;

    private String paymentMethod;
    private String paymentStatus;

    private String orderStatus;
    private String refundStatus;

    private BigDecimal subtotalBeforeDiscount;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;

    private LocalDateTime createdAt;

    public SalesOrder() {
    }

    public SalesOrder(
            LocalDate orderDate,
            Long productId,
            String customerName,
            String phone,
            String address,
            String productName,
            Integer quantity,
            String shippingMethod,
            String trackingNumber,
            String shippingStatus,
            String paymentMethod,
            String paymentStatus,
            String orderStatus,
            String refundStatus,
            BigDecimal subtotalBeforeDiscount,
            BigDecimal discountPercent,
            BigDecimal discountAmount,
            BigDecimal totalAmount
    ) {
        this.orderDate = orderDate;
        this.productId = productId;
        this.customerName = customerName;
        this.phone = phone;
        this.address = address;
        this.productName = productName;
        this.quantity = quantity;
        this.shippingMethod = shippingMethod;
        this.trackingNumber = trackingNumber;
        this.shippingStatus = shippingStatus;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.orderStatus = orderStatus;
        this.refundStatus = refundStatus;
        this.subtotalBeforeDiscount = subtotalBeforeDiscount;
        this.discountPercent = discountPercent;
        this.discountAmount = discountAmount;
        this.totalAmount = totalAmount;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public Long getProductId() {
        return productId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public String getShippingStatus() {
        return shippingStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public String getRefundStatus() {
        return refundStatus;
    }

    public BigDecimal getSubtotalBeforeDiscount() {
        return subtotalBeforeDiscount;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public void setShippingStatus(String shippingStatus) {
        this.shippingStatus = shippingStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setRefundStatus(String refundStatus) {
        this.refundStatus = refundStatus;
    }
}