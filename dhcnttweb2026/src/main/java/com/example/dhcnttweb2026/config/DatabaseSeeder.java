package com.example.dhcnttweb2026.config;

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
    private final DataSource dataSource;

    public DatabaseSeeder(ProductRepository productRepository, DataSource dataSource) {
        this.productRepository = productRepository;
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
    }
}
