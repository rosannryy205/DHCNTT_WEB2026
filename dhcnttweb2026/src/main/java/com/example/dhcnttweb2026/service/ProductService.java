package com.example.dhcnttweb2026.service;

import com.example.dhcnttweb2026.model.AdminProductDto;
import com.example.dhcnttweb2026.model.OrderRecord;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductService {
    private final List<AdminProductDto> products = new ArrayList<>();
    private final List<OrderRecord> orders = new ArrayList<>();
    private final AtomicLong idSequence = new AtomicLong(10);

    public ProductService() {
        products.add(new AdminProductDto(1L, "iPhone 17 Pro Titanium 256GB", "iphone-17-pro-titanium-256gb",
                "APL-IP17P-256", "iPhone", "Apple", "Titan Tự Nhiên", "256GB", "12GB",
                37990000L, 34990000L, 42, "Đang bán",
                "https://pngimg.com/uploads/iphone_14/iphone_14_PNG20.png",
                "Khung viền Titan cấp hàng không vũ trụ. Chip A19 Pro."));

        products.add(new AdminProductDto(2L, "MacBook Pro 16 inch M4 Max", "macbook-pro-16-inch-m4-max",
                "APL-MBP16-M4M", "Mac", "Apple", "Đen Không Gian (Space Black)", "1TB", "48GB",
                94990000L, 89990000L, 15, "Đang bán",
                "https://pngimg.com/uploads/macbook/macbook_PNG65.png",
                "Hiệu năng đỉnh cao cho kỹ sư và nhà phát triển chuyên nghiệp."));

        products.add(new AdminProductDto(3L, "iPad Pro 13 inch OLED M4", "ipad-pro-13-inch-oled-m4",
                "APL-IPADP13-M4", "iPad", "Apple", "Bạc (Silver)", "512GB", "16GB",
                41990000L, 37990000L, 28, "Đang bán",
                "https://pngimg.com/uploads/tablet/tablet_PNG8567.png",
                "Màn hình Ultra Retina XDR hai lớp OLED siêu mỏng 5.1 mm."));

        products.add(new AdminProductDto(4L, "AirPods Max USB-C", "airpods-max-usb-c",
                "APL-APM-USBC", "Phụ kiện", "Apple", "Midnight", "Không áp dụng", "Không áp dụng",
                14990000L, 13990000L, 34, "Đang bán",
                "https://pngimg.com/uploads/headphones/headphones_PNG101980.png",
                "Âm thanh độ trung thực cao cùng Chủ Động Khử Tiếng Ồn."));

        orders.add(new OrderRecord("#ORD-9042", "Nguyễn Minh Quân", "iPhone 17 Pro Titanium 256GB", "18/10/2026 09:15", 34990000L, "Hoàn tất"));
        orders.add(new OrderRecord("#ORD-9041", "Trần Thu Hà", "MacBook Pro 16 inch M4 Max", "18/10/2026 08:40", 89990000L, "Hoàn tất"));
        orders.add(new OrderRecord("#ORD-9040", "Lê Hoàng Nam", "iPad Pro 13 inch OLED M4", "17/10/2026 19:22", 37990000L, "Đang giao"));
        orders.add(new OrderRecord("#ORD-9039", "Phạm Bảo Ngọc", "AirPods Max USB-C", "17/10/2026 16:05", 13990000L, "Hoàn tất"));
    }

    public List<AdminProductDto> findAll(String keyword, String category) {
        return products.stream()
                .filter(p -> (category == null || category.isBlank() || "ALL".equalsIgnoreCase(category)
                        || (p.getCategory() != null && p.getCategory().equalsIgnoreCase(category))))
                .filter(p -> (keyword == null || keyword.isBlank()
                        || (p.getName() != null && p.getName().toLowerCase().contains(keyword.toLowerCase()))
                        || (p.getSku() != null && p.getSku().toLowerCase().contains(keyword.toLowerCase()))
                        || (p.getBrand() != null && p.getBrand().toLowerCase().contains(keyword.toLowerCase()))))
                .toList();
    }

    public Optional<AdminProductDto> findById(Long id) {
        return products.stream().filter(p -> Objects.equals(p.getId(), id)).findFirst();
    }

    public void save(AdminProductDto product) {
        if (product == null) {
            return;
        }
        if (product.getSlug() == null || product.getSlug().isBlank()) {
            product.setSlug(AdminProductDto.generateSlug(product.getName()));
        }
        if (product.getBrand() == null || product.getBrand().isBlank()) {
            product.setBrand("Apple");
        }
        if (product.getOriginalPrice() <= 0 && product.getPrice() > 0) {
            product.setOriginalPrice(product.getPrice());
        }

        if (product.getId() == null) {
            product.setId(idSequence.incrementAndGet());
            if (product.getImageUrl() == null || product.getImageUrl().isBlank()) {
                product.setImageUrl("https://pngimg.com/uploads/iphone_14/iphone_14_PNG20.png");
            }
            products.add(0, product);
        } else {
            findById(product.getId()).ifPresent(existing -> {
                existing.setName(product.getName());
                existing.setSlug(product.getSlug());
                existing.setSku(product.getSku());
                existing.setCategory(product.getCategory());
                existing.setBrand(product.getBrand());
                existing.setColor(product.getColor());
                existing.setStorage(product.getStorage());
                existing.setRam(product.getRam());
                existing.setOriginalPrice(product.getOriginalPrice());
                existing.setPrice(product.getPrice());
                existing.setStock(product.getStock());
                existing.setStatus(product.getStatus());
                existing.setTagline(product.getTagline());
                if (product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
                    existing.setImageUrl(product.getImageUrl());
                }
            });
        }
    }

    public void deleteById(Long id) {
        products.removeIf(p -> Objects.equals(p.getId(), id));
    }

    public List<OrderRecord> getRecentOrders() { return orders; }
    public long getTotalRevenue() { return 1482960000L; }
    public int getTotalOrdersCount() { return 342; }
    public int getTotalStock() {
        return products.stream()
                .filter(Objects::nonNull)
                .mapToInt(AdminProductDto::getStock)
                .sum();
    }
}
