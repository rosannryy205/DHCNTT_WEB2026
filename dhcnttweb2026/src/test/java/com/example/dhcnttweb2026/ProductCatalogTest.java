package com.example.dhcnttweb2026;

import com.example.dhcnttweb2026.controller.ClientProductController;
import com.example.dhcnttweb2026.service.ProductCatalogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.ExtendedModelMap;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProductCatalogTest {

    @Autowired
    private ProductCatalogService catalogService;

    @Autowired
    private ClientProductController productController;

    @Test
    void testCatalogServiceInitialData() {
        var products = catalogService.getAllProducts();
        assertThat(products).isNotEmpty();
        assertThat(products).hasSize(18);

        var filterOptions = catalogService.getFilterOptions();
        assertThat(filterOptions.categories()).hasSize(6);
        assertThat(filterOptions.storages()).isNotEmpty();
        assertThat(filterOptions.priceRanges()).isNotEmpty();
    }

    @Test
    void testFilterByCategoryAndPrice() {
        var result = catalogService.filterProducts(
                List.of("iphone"),
                20000000L,
                30000000L,
                null,
                null,
                "price-asc",
                1,
                12
        );

        assertThat(result.products()).isNotEmpty();
        for (var prod : result.products()) {
            assertThat(prod.category()).isEqualTo("iphone");
            assertThat(prod.startingPrice()).isBetween(20000000L, 30000000L);
        }
    }

    @Test
    void testControllerListProducts() {
        var model = new ExtendedModelMap();
        String view = productController.listProducts(
                List.of("mac"),
                null,
                null,
                null,
                null,
                "price-desc",
                1,
                model
        );

        assertThat(view).isEqualTo("client/products");
        assertThat(model.get("catalogResult")).isNotNull();
        assertThat(model.get("filterOptions")).isNotNull();
    }

    @Test
    void testControllerFilterFragment() {
        var model = new ExtendedModelMap();
        String view = productController.filterProductsFragment(
                List.of("iphone"),
                null,
                null,
                null,
                null,
                "newest",
                1,
                model
        );

        assertThat(view).isEqualTo("client/fragments/product-grid :: product-grid");
        assertThat(model.get("catalogResult")).isNotNull();
    }

    @Test
    void testCategoryRedirect() {
        String redirect = productController.categoryRedirect("iphone");
        assertThat(redirect).isEqualTo("redirect:/products?category=iphone");
    }
}
