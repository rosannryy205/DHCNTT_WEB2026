package com.example.dhcnttweb2026.config;

import com.example.dhcnttweb2026.entity.Category;
import com.example.dhcnttweb2026.repository.CategoryRepository;
import com.example.dhcnttweb2026.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final DataSource dataSource;

    public DatabaseSeeder(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          DataSource dataSource) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        long count = productRepository.count();
        log.info("DatabaseSeeder: existing products count = {}", count);
        if (count == 0) {
            log.info("Database is empty. Seeding initial data from sample_data.sql...");
            try {
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                populator.addScript(new ClassPathResource("sample_data.sql"));
                populator.setContinueOnError(true);
                populator.setSeparator(";");
                populator.execute(dataSource);
                log.info("Seeding completed. Total products now = {}", productRepository.count());
            } catch (Exception e) {
                log.error("Failed to seed database from sample_data.sql", e);
            }
        }

        seedMissingSubCategories();
    }

    private void seedMissingSubCategories() {
        try {
            // Kiểm tra và bổ sung danh mục con cho Watch nếu chưa có
            categoryRepository.findBySlug("watch").ifPresent(watch -> {
                if (categoryRepository.findBySlug("apple-watch-ultra-2").isEmpty()) {
                    categoryRepository.save(new Category("Apple Watch Ultra 2", "apple-watch-ultra-2", watch,
                            "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/ultra-case-unselect-gallery-1-202409?wid=800&hei=600&fmt=jpeg&qlt=90", true));
                }
                if (categoryRepository.findBySlug("apple-watch-series-10").isEmpty()) {
                    categoryRepository.save(new Category("Apple Watch Series 10", "apple-watch-series-10", watch,
                            "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/s10-case-unselect-gallery-1-202409?wid=800&hei=600&fmt=jpeg&qlt=90", true));
                }
            });

            // Kiểm tra và bổ sung danh mục con cho AirPods nếu chưa có
            categoryRepository.findBySlug("airpods").ifPresent(airpods -> {
                if (categoryRepository.findBySlug("airpods-pro-2").isEmpty()) {
                    categoryRepository.save(new Category("AirPods Pro (Gen 2)", "airpods-pro-2", airpods,
                            "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/airpods-pro-2-hero-select-202409?wid=800&hei=600&fmt=jpeg&qlt=90", true));
                }
                if (categoryRepository.findBySlug("airpods-max-usbc").isEmpty()) {
                    categoryRepository.save(new Category("AirPods Max", "airpods-max-usbc", airpods,
                            "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/airpods-max-select-202409-midnight?wid=800&hei=600&fmt=jpeg&qlt=90", true));
                }
            });

            // Kiểm tra và bổ sung danh mục con cho Phụ Kiện nếu chưa có
            categoryRepository.findBySlug("phu-kien").ifPresent(phuKien -> {
                if (categoryRepository.findBySlug("cu-sac-nhanh-apple-30w-usb-c").isEmpty()) {
                    categoryRepository.save(new Category("Sạc & Cáp Nhanh", "cu-sac-nhanh-apple-30w-usb-c", phuKien,
                            "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/MU7T2_GEO_VN?wid=800&hei=600&fmt=jpeg&qlt=90", true));
                }
                if (categoryRepository.findBySlug("op-lung-silicone-magsafe-iphone-16-pro").isEmpty()) {
                    categoryRepository.save(new Category("Ốp lưng & Bao da", "op-lung-silicone-magsafe-iphone-16-pro", phuKien,
                            "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/MC8K4?wid=800&hei=600&fmt=jpeg&qlt=90", true));
                }
            });
        } catch (Exception e) {
            log.warn("Notice: could not seed additional subcategories: {}", e.getMessage());
        }
    }
}
