package com.example.dhcnttweb2026.service;

import com.example.dhcnttweb2026.entity.*;
import com.example.dhcnttweb2026.model.AdminCategoryDto;
import com.example.dhcnttweb2026.model.AdminProductDto;
import com.example.dhcnttweb2026.model.OrderRecord;
import com.example.dhcnttweb2026.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@SuppressWarnings("null")
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;

    public ProductService(ProductRepository productRepository,
                          ProductVariantRepository productVariantRepository,
                          CategoryRepository categoryRepository,
                          BrandRepository brandRepository,
                          OrderItemRepository orderItemRepository,
                          CartItemRepository cartItemRepository,
                          OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * Lấy danh sách sản phẩm từ CSDL MySQL (products & product_variants).
     * Hỗ trợ tìm kiếm theo từ khóa (tên, SKU, hãng) và lọc theo danh mục (tên, slug, hoặc ID).
     */
    @Transactional(readOnly = true)
    public List<AdminProductDto> findAll(String keyword, String category) {
        List<Product> allProducts = productRepository.findAllWithCategoryBrandAndVariants();

        List<AdminProductDto> result = new ArrayList<>();
        for (Product p : allProducts) {
            if (p == null) continue;

            // Lọc theo danh mục nếu có
            if (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) {
                String catName = (p.getCategory() != null) ? p.getCategory().getName() : "";
                String catSlug = (p.getCategory() != null) ? p.getCategory().getSlug() : "";
                String catIdStr = (p.getCategory() != null && p.getCategory().getId() != null) ? String.valueOf(p.getCategory().getId()) : "";
                if (!catName.equalsIgnoreCase(category) && !catSlug.equalsIgnoreCase(category) && !catIdStr.equals(category)) {
                    continue;
                }
            }

            // Lấy biến thể đầu tiên làm đại diện hiển thị trong danh sách
            List<ProductVariant> variants = p.getVariants();
            ProductVariant pv = (variants != null && !variants.isEmpty()) ? variants.get(0) : null;

            String sku = (pv != null && pv.getSku() != null) ? pv.getSku() : ("PROD-" + p.getId());
            String brandName = (p.getBrand() != null) ? p.getBrand().getName() : "Apple";
            String categoryName = (p.getCategory() != null) ? p.getCategory().getName() : "Chưa phân loại";
            String color = (pv != null && pv.getColor() != null) ? pv.getColor() : "Tiêu chuẩn";
            String storage = (pv != null && pv.getStorage() != null) ? pv.getStorage() : "Tiêu chuẩn";
            String ram = (pv != null && pv.getRam() != null) ? pv.getRam() : "Tiêu chuẩn";

            long price = (pv != null && pv.getPrice() != null) ? pv.getPrice().longValue() : 0L;
            long origPrice = (pv != null && pv.getOriginalPrice() != null) ? pv.getOriginalPrice().longValue() : price;
            int stock = (pv != null && pv.getStock() != null) ? pv.getStock() : 0;
            String img = (pv != null && pv.getImage() != null && !pv.getImage().isBlank())
                    ? pv.getImage()
                    : "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-model-unselect-gallery-2-202409?wid=800&hei=600&fmt=jpeg&qlt=90";

            String status = Boolean.TRUE.equals(p.getIsActive()) ? "Đang bán" : "Ngừng kinh doanh";

            // Lọc theo từ khóa tìm kiếm (tên sản phẩm, SKU, hãng)
            if (keyword != null && !keyword.isBlank()) {
                String kw = keyword.toLowerCase().trim();
                boolean matchName = p.getName() != null && p.getName().toLowerCase().contains(kw);
                boolean matchSku = sku.toLowerCase().contains(kw);
                boolean matchBrand = brandName.toLowerCase().contains(kw);
                if (!matchName && !matchSku && !matchBrand) {
                    continue;
                }
            }

            AdminProductDto dto = new AdminProductDto(
                    p.getId(),
                    p.getName(),
                    p.getSlug(),
                    sku,
                    categoryName,
                    brandName,
                    color,
                    storage,
                    ram,
                    origPrice,
                    price,
                    stock,
                    status,
                    img,
                    p.getDescription()
            );
            if (p.getCategory() != null) dto.setCategoryId(p.getCategory().getId());
            if (p.getBrand() != null) dto.setBrandId(p.getBrand().getId());
            if (pv != null) dto.setVariantId(pv.getId());

            result.add(dto);
        }

        return result;
    }

    /**
     * Tìm sản phẩm theo ID kèm đầy đủ biến thể và quan hệ để nạp vào form chỉnh sửa.
     */
    @Transactional(readOnly = true)
    public Optional<AdminProductDto> findById(Long id) {
        if (id == null) return Optional.empty();

        return productRepository.findByIdWithDetails(id).map(p -> {
            List<ProductVariant> variants = p.getVariants();
            ProductVariant pv = (variants != null && !variants.isEmpty()) ? variants.get(0) : null;

            String sku = (pv != null && pv.getSku() != null) ? pv.getSku() : ("PROD-" + p.getId());
            String brandName = (p.getBrand() != null) ? p.getBrand().getName() : "Apple";
            String categoryName = (p.getCategory() != null) ? p.getCategory().getName() : "iPhone";
            String color = (pv != null && pv.getColor() != null) ? pv.getColor() : "";
            String storage = (pv != null && pv.getStorage() != null) ? pv.getStorage() : "";
            String ram = (pv != null && pv.getRam() != null) ? pv.getRam() : "";

            long price = (pv != null && pv.getPrice() != null) ? pv.getPrice().longValue() : 0L;
            long origPrice = (pv != null && pv.getOriginalPrice() != null) ? pv.getOriginalPrice().longValue() : price;
            int stock = (pv != null && pv.getStock() != null) ? pv.getStock() : 0;
            String img = (pv != null && pv.getImage() != null) ? pv.getImage() : "";
            String status = Boolean.TRUE.equals(p.getIsActive()) ? "Đang bán" : "Ngừng kinh doanh";

            AdminProductDto dto = new AdminProductDto(
                    p.getId(),
                    p.getName(),
                    p.getSlug(),
                    sku,
                    categoryName,
                    brandName,
                    color,
                    storage,
                    ram,
                    origPrice,
                    price,
                    stock,
                    status,
                    img,
                    p.getDescription()
            );
            if (p.getCategory() != null) dto.setCategoryId(p.getCategory().getId());
            if (p.getBrand() != null) dto.setBrandId(p.getBrand().getId());
            if (pv != null) dto.setVariantId(pv.getId());

            return dto;
        });
    }

    /**
     * Lưu thật sản phẩm và biến thể vào CSDL MySQL:
     * 1. Xác thực các trường bắt buộc, giá không âm, số lượng không âm
     * 2. Kiểm tra tính duy nhất của SKU trên bảng product_variants
     * 3. Kiểm tra tính duy nhất của Slug trên bảng products
     * 4. Ràng buộc khóa ngoại category_id và brand_id
     * 5. Lưu bản ghi Product vào bảng `products`
     * 6. Lưu bản ghi ProductVariant vào bảng `product_variants`
     */
    @Transactional
    public void save(AdminProductDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Dữ liệu sản phẩm không được rỗng.");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Tên sản phẩm là trường bắt buộc.");
        }

        // 1. Kiểm tra giá bán và số lượng không âm
        if (dto.getPrice() < 0) {
            throw new IllegalArgumentException("Giá bán không được là số âm (>= 0).");
        }
        if (dto.getOriginalPrice() < 0) {
            throw new IllegalArgumentException("Giá gốc không được là số âm (>= 0).");
        }
        if (dto.getStock() < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không được là số âm (>= 0).");
        }

        long price = dto.getPrice();
        long origPrice = (dto.getOriginalPrice() > 0) ? dto.getOriginalPrice() : price;
        int stock = dto.getStock();
        dto.setPrice(price);
        dto.setOriginalPrice(origPrice);
        dto.setStock(stock);

        // 2. Chuẩn hóa và xử lý Slug không trùng lặp
        String rawSlug = (dto.getSlug() != null && !dto.getSlug().isBlank())
                ? dto.getSlug().trim()
                : AdminProductDto.generateSlug(dto.getName());
        String slug = AdminProductDto.generateSlug(rawSlug);
        if (slug.isBlank()) {
            slug = "san-pham-" + System.currentTimeMillis() % 100000;
        }

        if (dto.getId() == null) {
            if (productRepository.existsBySlug(slug)) {
                slug = slug + "-" + (System.currentTimeMillis() % 10000);
            }
        } else {
            if (productRepository.existsBySlugAndIdNot(slug, dto.getId())) {
                slug = slug + "-" + (System.currentTimeMillis() % 10000);
            }
        }
        dto.setSlug(slug);

        // 3. Kiểm tra SKU bắt buộc và duy nhất
        String sku = (dto.getSku() != null && !dto.getSku().isBlank())
                ? dto.getSku().trim()
                : ("APL-" + System.currentTimeMillis() % 100000);
        dto.setSku(sku);

        // 4. Xác định Danh mục (Category FK)
        Category category = null;
        if (dto.getCategoryId() != null && dto.getCategoryId() > 0) {
            category = categoryRepository.findById(dto.getCategoryId()).orElse(null);
        }
        if (category == null && dto.getCategory() != null && !dto.getCategory().isBlank()) {
            category = categoryRepository.findBySlug(AdminCategoryDto.generateSlug(dto.getCategory())).orElse(null);
            if (category == null) {
                category = categoryRepository.findAll().stream()
                        .filter(c -> c.getName().equalsIgnoreCase(dto.getCategory().trim()))
                        .findFirst().orElse(null);
            }
        }
        if (category == null) {
            category = categoryRepository.findAll().stream().findFirst()
                    .orElseGet(() -> categoryRepository.save(new Category("Thiết bị Apple", "thiet-bi-apple", null, "", true)));
        }

        // Không cho phép chọn danh mục đã bị vô hiệu hóa khi tạo sản phẩm mới
        if (dto.getId() == null && Boolean.FALSE.equals(category.getIsActive())) {
            throw new IllegalArgumentException("Danh mục '" + category.getName() + "' hiện đang bị vô hiệu hóa. Vui lòng chọn danh mục đang hoạt động.");
        }

        dto.setCategoryId(category.getId());
        dto.setCategory(category.getName());

        // 5. Xác định Thương hiệu (Brand FK)
        Brand brand = null;
        if (dto.getBrandId() != null && dto.getBrandId() > 0) {
            brand = brandRepository.findById(dto.getBrandId()).orElse(null);
        }
        if (brand == null && dto.getBrand() != null && !dto.getBrand().isBlank()) {
            brand = brandRepository.findByNameIgnoreCase(dto.getBrand().trim()).orElse(null);
        }
        if (brand == null) {
            brand = brandRepository.findAll().stream().findFirst()
                    .orElseGet(() -> brandRepository.save(new Brand(null, "Apple", "apple", "https://upload.wikimedia.org/wikipedia/commons/f/fa/Apple_logo_black.svg", "Tập đoàn Apple")));
        }
        dto.setBrandId(brand.getId());
        dto.setBrand(brand.getName());

        boolean isActive = !"Ngừng kinh doanh".equalsIgnoreCase(dto.getStatus()) && !"Tạm hết hàng".equalsIgnoreCase(dto.getStatus());
        String img = (dto.getImageUrl() != null && !dto.getImageUrl().isBlank())
                ? dto.getImageUrl().trim()
                : "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-model-unselect-gallery-2-202409?wid=800&hei=600&fmt=jpeg&qlt=90";
        if (img.length() > 255) {
            img = img.substring(0, 255);
        }

        // Mô tả an toàn tối đa 255 ký tự (phù hợp cột description VARCHAR(255))
        String description = dto.getTagline();
        if (description != null && description.length() > 255) {
            description = description.substring(0, 255);
        }

        // 6. Xử lý Thêm mới hoặc Cập nhật vào CSDL
        if (dto.getId() == null) {
            // Kiểm tra trùng SKU khi thêm mới
            if (productVariantRepository.existsBySku(sku)) {
                throw new IllegalArgumentException("Mã SKU '" + sku + "' đã tồn tại trong CSDL. Vui lòng nhập mã SKU khác.");
            }

            // Tạo Product
            Product product = new Product(category, brand, dto.getName().trim(), slug, description, isActive);
            product = productRepository.save(product);

            // Tạo ProductVariant
            ProductVariant variant = new ProductVariant(
                    product,
                    sku,
                    dto.getColor() != null ? dto.getColor().trim() : "Tiêu chuẩn",
                    dto.getStorage() != null ? dto.getStorage().trim() : "Tiêu chuẩn",
                    dto.getRam() != null ? dto.getRam().trim() : "Tiêu chuẩn",
                    BigDecimal.valueOf(origPrice),
                    BigDecimal.valueOf(price),
                    stock,
                    img
            );
            productVariantRepository.save(variant);

            dto.setId(product.getId());
            dto.setVariantId(variant.getId());
        } else {
            // Cập nhật Product hiện có
            Product product = productRepository.findById(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm có ID: " + dto.getId()));

            product.setCategory(category);
            product.setBrand(brand);
            product.setName(dto.getName().trim());
            product.setSlug(slug);
            product.setDescription(description);
            product.setIsActive(isActive);
            productRepository.save(product);

            // Cập nhật ProductVariant
            List<ProductVariant> variants = productVariantRepository.findByProductId(product.getId());
            ProductVariant variant = null;
            if (dto.getVariantId() != null) {
                variant = productVariantRepository.findById(dto.getVariantId()).orElse(null);
            }
            if (variant == null) {
                variant = !variants.isEmpty() ? variants.get(0) : null;
            }

            if (variant != null) {
                // Kiểm tra trùng SKU khi sửa
                if (productVariantRepository.existsBySkuAndIdNot(sku, variant.getId())) {
                    throw new IllegalArgumentException("Mã SKU '" + sku + "' đã được sử dụng bởi phiên bản sản phẩm khác.");
                }
                variant.setSku(sku);
                if (dto.getColor() != null) variant.setColor(dto.getColor().trim());
                if (dto.getStorage() != null) variant.setStorage(dto.getStorage().trim());
                if (dto.getRam() != null) variant.setRam(dto.getRam().trim());
                variant.setOriginalPrice(BigDecimal.valueOf(origPrice));
                variant.setPrice(BigDecimal.valueOf(price));
                variant.setStock(stock);
                if (dto.getImageUrl() != null && !dto.getImageUrl().isBlank()) {
                    variant.setImage(img);
                }
                productVariantRepository.save(variant);
            } else {
                // Nếu sản phẩm trước đó chưa có variant thì tạo mới
                if (productVariantRepository.existsBySku(sku)) {
                    throw new IllegalArgumentException("Mã SKU '" + sku + "' đã tồn tại trong CSDL. Vui lòng nhập mã SKU khác.");
                }
                variant = new ProductVariant(
                        product,
                        sku,
                        dto.getColor() != null ? dto.getColor().trim() : "Tiêu chuẩn",
                        dto.getStorage() != null ? dto.getStorage().trim() : "Tiêu chuẩn",
                        dto.getRam() != null ? dto.getRam().trim() : "Tiêu chuẩn",
                        BigDecimal.valueOf(origPrice),
                        BigDecimal.valueOf(price),
                        stock,
                        img
                );
                productVariantRepository.save(variant);
            }
        }
    }

    /**
     * Xóa sản phẩm an toàn:
     * - Nếu sản phẩm đã từng phát sinh trong đơn hàng (order_items), KHÔNG xóa cứng để tránh mất dữ liệu lịch sử
     *   mà tự động chuyển sang soft delete (isActive = false).
     * - Nếu sản phẩm chưa có đơn hàng: xóa sạch trong giỏ hàng (cart_items) và xóa sản phẩm khỏi CSDL.
     */
    @Transactional
    public String deleteById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm có ID: " + id));

        // Kiểm tra xem sản phẩm hoặc bất kỳ biến thể nào đã có đơn hàng chưa
        boolean hasOrders = orderItemRepository.existsByProductId(id);
        List<ProductVariant> variants = productVariantRepository.findByProductId(id);
        if (!hasOrders) {
            for (ProductVariant pv : variants) {
                if (orderItemRepository.existsByProductVariantId(pv.getId())) {
                    hasOrders = true;
                    break;
                }
            }
        }

        if (hasOrders) {
            // Ngừng kinh doanh (Soft delete) để bảo toàn tính toàn vẹn dữ liệu đơn hàng
            product.setIsActive(false);
            productRepository.save(product);
            return "Sản phẩm '" + product.getName() + "' đã có lịch sử trong đơn hàng nên được chuyển sang trạng thái 'Ngừng kinh doanh' để bảo toàn dữ liệu.";
        }

        // Xóa các dòng tham chiếu trong giỏ hàng nếu có
        for (ProductVariant pv : variants) {
            cartItemRepository.deleteByProductVariantId(pv.getId());
        }

        // Xóa các biến thể trực tiếp
        productVariantRepository.deleteByProductId(id);

        // Xóa sản phẩm
        productRepository.delete(product);
        return "Đã xóa hoàn toàn sản phẩm '" + product.getName() + "' khỏi hệ thống CSDL.";
    }

    // Các thống kê Dashboard từ CSDL thật
    @Transactional(readOnly = true)
    public long getTotalRevenue() {
        return orderRepository.findAll().stream()
                .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.PAID)
                .mapToLong(o -> o.getTotalAmount() != null ? o.getTotalAmount().longValue() : 0L)
                .sum();
    }

    @Transactional(readOnly = true)
    public int getTotalOrdersCount() {
        return (int) orderRepository.count();
    }

    @Transactional(readOnly = true)
    public int getTotalStock() {
        return productVariantRepository.findAll().stream()
                .mapToInt(pv -> pv.getStock() != null ? pv.getStock() : 0)
                .sum();
    }

    @Transactional(readOnly = true)
    public List<OrderRecord> getRecentOrders() {
        List<Order> orders = orderRepository.findAll();
        List<OrderRecord> records = new ArrayList<>();
        int count = 0;
        for (int i = orders.size() - 1; i >= 0 && count < 5; i--, count++) {
            Order o = orders.get(i);
            String custName = (o.getUser() != null && o.getUser().getName() != null) ? o.getUser().getName() : "Khách hàng";
            String date = (o.getCreatedAt() != null) ? o.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Mới đây";
            long total = (o.getTotalAmount() != null) ? o.getTotalAmount().longValue() : 0L;
            String status = (o.getOrderStatus() != null) ? o.getOrderStatus().name() : "Pending";

            records.add(new OrderRecord(
                    o.getOrderCode(),
                    custName,
                    "Đơn hàng #" + o.getId(),
                    date,
                    total,
                    status
            ));
        }
        return records;
    }
}
