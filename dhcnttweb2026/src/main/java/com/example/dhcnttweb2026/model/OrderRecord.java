package com.example.dhcnttweb2026.model;

public class OrderRecord {
    private String orderCode;
    private String customerName;
    private String productName;
    private String orderDate;
    private long totalAmount;
    private String status;

    public OrderRecord(String orderCode, String customerName, String productName, String orderDate, long totalAmount, String status) {
        this.orderCode = orderCode;
        this.customerName = customerName;
        this.productName = productName;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public String getOrderCode() { return orderCode; }
    public String getCustomerName() { return customerName; }
    public String getProductName() { return productName; }
    public String getOrderDate() { return orderDate; }
    public long getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
}