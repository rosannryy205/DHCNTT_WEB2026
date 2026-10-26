package com.example.dhcnttweb2026;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class AdminControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(Objects.requireNonNull(webApplicationContext)).build();
    }

    @Test
    void testAdminTrailingSlashUrlRendersSuccessfully() throws Exception {
        mockMvc.perform(get("/admin/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("activeMenu", "dashboard"));
    }

    @Test
    void testProductListPageHasAddButtonAndNoSidebarAddEdit() throws Exception {
        var result = mockMvc.perform(get("/admin/products"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("activeMenu", "products"))
                .andReturn();

        String html = result.getResponse().getContentAsString();

        // 1. Sidebar should NOT have standalone Add or Edit links
        // We verify the sidebar navigation doesn't link to add/edit as separate menu items
        assertFalse(html.contains("<a href=\"/admin/products/edit/1\" class=\"sidebar-link"), 
                "Sidebar must not contain a hardcoded edit product link");
        assertFalse(html.contains("<a href=\"/admin/products/add\" class=\"sidebar-link"), 
                "Sidebar must not contain a standalone add product link");

        // 2. Main content must have "+ Thêm sản phẩm" button
        assertTrue(html.contains("/admin/products/add"), 
                "Product list page must contain a link/button to add product");

        // 3. Main content must have Edit links for products
        assertTrue(html.contains("/admin/products/edit/"), 
                "Product list page must contain edit links for individual products");
    }

    @Test
    void testAddProductPageActiveMenuAndAllDatabaseFields() throws Exception {
        var result = mockMvc.perform(get("/admin/products/add"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("activeMenu", "products"))
                .andReturn();

        String html = result.getResponse().getContentAsString();

        // Check for all database fields in the form
        assertTrue(html.contains("name=\"name\""), "Form must have product name field");
        assertTrue(html.contains("name=\"slug\""), "Form must have slug field (Products table)");
        assertTrue(html.contains("name=\"category\""), "Form must have category field (Products table)");
        assertTrue(html.contains("name=\"brand\""), "Form must have brand field (Products table)");
        assertTrue(html.contains("name=\"sku\""), "Form must have SKU field (Product_Variants table)");
        assertTrue(html.contains("name=\"color\""), "Form must have color field (Product_Variants table)");
        assertTrue(html.contains("name=\"storage\""), "Form must have storage field (Product_Variants table)");
        assertTrue(html.contains("name=\"ram\""), "Form must have RAM field (Product_Variants table)");
        assertTrue(html.contains("name=\"originalPrice\""), "Form must have originalPrice field (Product_Variants table)");
        assertTrue(html.contains("name=\"price\""), "Form must have price field (Product_Variants table)");
        assertTrue(html.contains("name=\"stock\""), "Form must have stock field (Product_Variants table)");
        assertTrue(html.contains("name=\"status\""), "Form must have status/isActive field");
        assertTrue(html.contains("name=\"imageUrl\""), "Form must have image URL field");
        assertTrue(html.contains("name=\"tagline\"") || html.contains("name=\"description\""), "Form must have description field");
    }

    @Test
    void testPostAddProductSavesAllDatabaseFieldsAndRedirects() throws Exception {
        mockMvc.perform(post("/admin/products/add")
                        .param("name", "Mac Studio M4 Max")
                        .param("slug", "mac-studio-m4-max")
                        .param("category", "Mac")
                        .param("brand", "Apple")
                        .param("sku", "APL-MS-M4M-64")
                        .param("color", "Bạc Silver")
                        .param("storage", "1TB")
                        .param("ram", "64GB")
                        .param("originalPrice", "65990000")
                        .param("price", "59990000")
                        .param("stock", "10")
                        .param("status", "Đang bán")
                        .param("imageUrl", "https://example.com/mac-studio.png")
                        .param("tagline", "Sức mạnh phi thường trong thiết kế nhỏ gọn."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"));
    }

    @Test
    void testEditProductPageActiveMenuAndAllDatabaseFields() throws Exception {
        var result = mockMvc.perform(get("/admin/products/edit/1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("activeMenu", "products"))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertTrue(html.contains("name=\"name\""), "Edit form must have product name field");
        assertTrue(html.contains("name=\"slug\""), "Edit form must have slug field");
        assertTrue(html.contains("name=\"brand\""), "Edit form must have brand field");
        assertTrue(html.contains("name=\"color\""), "Edit form must have color field");
        assertTrue(html.contains("name=\"storage\""), "Edit form must have storage field");
        assertTrue(html.contains("name=\"ram\""), "Edit form must have RAM field");
        assertTrue(html.contains("name=\"originalPrice\""), "Edit form must have originalPrice field");
    }
}
