package com.example.dhcnttweb2026.model;

/**
 * Form DTO chứa toàn bộ dữ liệu người dùng nhập tại giao diện thanh toán (Checkout).
 * Tuân thủ đầy đủ các trường và độ dài trong Database:
 * - users.name (max 100)
 * - users.email (max 255)
 * - orders.shipping_phone (max 20)
 * - orders.shipping_address (max 255)
 * - orders.note (max 255)
 * - orders.payment_method (enum: cod, vnpay, momo)
 */
public class CheckoutFormDto {

    // 1. Thông tin người nhận hàng (users table & contact)
    private String recipientName;
    private String email;
    private String shippingPhone;

    // 2. Địa chỉ giao hàng chi tiết (orders.shipping_address)
    private String province;
    private String district;
    private String ward;
    private String streetAddress;
    private String shippingAddress;

    // Phương thức vận chuyển
    private String shippingMethod = "standard"; // standard: Giao tiêu chuẩn, express: Hỏa tốc

    // 3. Ghi chú đơn hàng (orders.note - max 255 ký tự)
    private String note;

    // 4. Phương thức thanh toán (orders.payment_method: cod, vnpay, momo)
    private String paymentMethod = "cod";

    // 5. Tùy chọn xuất hóa đơn GTGT (VAT)
    private boolean needVatInvoice;
    private String companyName;
    private String taxCode;
    private String companyAddress;
    private String invoiceEmail;

    // Mã khuyến mãi / Voucher
    private String couponCode;

    // Tổng tiền thanh toán (orders.total_amount)
    private Long totalAmount;

    public CheckoutFormDto() {
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getShippingPhone() {
        return shippingPhone;
    }

    public void setShippingPhone(String shippingPhone) {
        this.shippingPhone = shippingPhone;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean isNeedVatInvoice() {
        return needVatInvoice;
    }

    public void setNeedVatInvoice(boolean needVatInvoice) {
        this.needVatInvoice = needVatInvoice;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public String getInvoiceEmail() {
        return invoiceEmail;
    }

    public void setInvoiceEmail(String invoiceEmail) {
        this.invoiceEmail = invoiceEmail;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }
}

