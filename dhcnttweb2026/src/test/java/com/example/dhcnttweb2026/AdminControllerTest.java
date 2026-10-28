package com.example.dhcnttweb2026;

import com.example.dhcnttweb2026.entity.*;
import com.example.dhcnttweb2026.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@SuppressWarnings("null")
class AdminControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(Objects.requireNonNull(webApplicationContext)).build();
    }

    // =========================================================================
    // 1. DASHBOARD & MENU TESTS
    // =========================================================================
    @Test
    void testAdminTrailingSlashUrlRendersSuccessfully() throws Exception {
        mockMvc.perform(get("/admin/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("activeMenu", "dashboard"));
    }

    // =========================================================================
    // 2. PRODUCT MANAGEMENT TESTS
    // =========================================================================
    @Test
    void testProductListPageHasAddButtonAndNoSidebarAddEdit() throws Exception {
        var result = mockMvc.perform(get("/admin/products"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("activeMenu", "products"))
                .andReturn();

        String html = result.getResponse().getContentAsString();

        // 1. Sidebar should NOT have standalone Add or Edit links
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
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("brands"))
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
        String uniqueSku = "TEST-SKU-" + System.currentTimeMillis();

        mockMvc.perform(post("/admin/products/add")
                        .param("name", "Mac Studio M4 Max Test")
                        .param("slug", "mac-studio-m4-max-test")
                        .param("category", "Mac")
                        .param("brand", "Apple")
                        .param("sku", uniqueSku)
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
                .andExpect(redirectedUrl("/admin/products"))
                .andExpect(flash().attributeExists("successMessage"));

        // Xác nhận bản ghi đã thực sự được lưu vào CSDL MySQL (bảng products & product_variants)
        Optional<Product> prodOpt = productRepository.findBySlug("mac-studio-m4-max-test");
        assertTrue(prodOpt.isPresent(), "Product must be saved into real MySQL products table");
        assertEquals("Mac Studio M4 Max Test", prodOpt.get().getName());

        Optional<ProductVariant> varOpt = productVariantRepository.findBySku(uniqueSku);
        assertTrue(varOpt.isPresent(), "ProductVariant must be saved into real MySQL product_variants table");
        assertEquals(10, varOpt.get().getStock());
        assertEquals(0, BigDecimal.valueOf(59990000).compareTo(varOpt.get().getPrice()));
    }

    @Test
    void testPostAddProductDuplicateSkuFailsValidation() throws Exception {
        // Lấy một SKU đang có trong CSDL
        ProductVariant existingVar = productVariantRepository.findAll().stream().findFirst().orElseThrow();
        String duplicateSku = existingVar.getSku();

        mockMvc.perform(post("/admin/products/add")
                        .param("name", "Sản phẩm SKU trùng")
                        .param("slug", "san-pham-sku-trung")
                        .param("category", "iPhone")
                        .param("brand", "Apple")
                        .param("sku", duplicateSku)
                        .param("price", "20000000")
                        .param("stock", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products/add"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void testEditProductPageActiveMenuAndAllDatabaseFields() throws Exception {
        Product p = productRepository.findAll().stream().findFirst().orElseThrow();

        var result = mockMvc.perform(get("/admin/products/edit/" + p.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("activeMenu", "products"))
                .andExpect(model().attributeExists("product"))
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

    @Test
    void testPostEditProductUpdatesDatabaseRecord() throws Exception {
        Product p = productRepository.findAll().stream().findFirst().orElseThrow();
        Long prodId = p.getId();
        String updatedName = "Tên cập nhật " + System.currentTimeMillis();

        mockMvc.perform(post("/admin/products/edit/" + prodId)
                        .param("id", String.valueOf(prodId))
                        .param("name", updatedName)
                        .param("slug", p.getSlug())
                        .param("category", p.getCategory().getName())
                        .param("brand", p.getBrand().getName())
                        .param("sku", "SKU-EDIT-" + System.currentTimeMillis())
                        .param("price", "25000000")
                        .param("stock", "15")
                        .param("status", "Đang bán"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"))
                .andExpect(flash().attributeExists("successMessage"));

        Product reloaded = productRepository.findById(prodId).orElseThrow();
        assertEquals(updatedName, reloaded.getName(), "Product name must be updated in database");
    }

    @Test
    void testDeleteProductReferencedInOrdersSwitchesToInactive() throws Exception {
        // Tìm sản phẩm có trong order_items
        OrderItem oi = orderItemRepository.findAll().stream().findFirst().orElseThrow();
        Product productInOrder = oi.getProductVariant().getProduct();
        Long prodId = productInOrder.getId();

        mockMvc.perform(post("/admin/products/delete/" + prodId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"))
                .andExpect(flash().attributeExists("successMessage"));

        // Xác nhận sản phẩm không bị xóa cứng (làm hỏng đơn hàng) mà được chuyển sang isActive = false
        Product reloaded = productRepository.findById(prodId).orElseThrow();
        assertFalse(reloaded.getIsActive(), "Product with order history must be soft-deleted (isActive = false)");
    }

    // =========================================================================
    // 3. CATEGORY MANAGEMENT TESTS
    // =========================================================================
    @Test
    void testCategoryListPageLoadsSuccessfully() throws Exception {
        var result = mockMvc.perform(get("/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("activeMenu", "categories"))
                .andExpect(model().attributeExists("categories"))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertTrue(html.contains("Quản Lý Danh Mục"), "Page should render category header");
        assertTrue(html.contains("/admin/categories/add"), "Page must contain link to add category");
    }

    @Test
    void testAddCategoryPageLoadsWithAllDatabaseFields() throws Exception {
        var result = mockMvc.perform(get("/admin/categories/add"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("activeMenu", "categories"))
                .andExpect(model().attributeExists("category"))
                .andExpect(model().attributeExists("rootCategories"))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertTrue(html.contains("name=\"name\""), "Category form must have name field");
        assertTrue(html.contains("name=\"slug\""), "Category form must have slug field");
        assertTrue(html.contains("name=\"parentId\""), "Category form must have parent_id field");
        assertTrue(html.contains("name=\"image\""), "Category form must have image field");
        assertTrue(html.contains("name=\"isActive\""), "Category form must have is_active field");
    }

    @Test
    void testPostAddCategoryValidSavesToDatabaseAndRedirects() throws Exception {
        String catName = "Vision Pro " + System.currentTimeMillis();
        String catSlug = "vision-pro-" + System.currentTimeMillis();

        mockMvc.perform(post("/admin/categories/add")
                        .param("name", catName)
                        .param("slug", catSlug)
                        .param("image", "https://example.com/vision-pro.png")
                        .param("isActive", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"))
                .andExpect(flash().attributeExists("successMessage"));

        Optional<Category> saved = categoryRepository.findBySlug(catSlug);
        assertTrue(saved.isPresent(), "Category must be saved into real MySQL categories table");
        assertEquals(catName, saved.get().getName());
        assertTrue(saved.get().getIsActive());
    }

    @Test
    void testPostAddCategoryMissingNameFailsAndRedirects() throws Exception {
        mockMvc.perform(post("/admin/categories/add")
                        .param("name", "")
                        .param("slug", "invalid-slug"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories/add"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void testPostAddCategoryDuplicateNameFails() throws Exception {
        Category existing = categoryRepository.findAll().stream().findFirst().orElseThrow();

        mockMvc.perform(post("/admin/categories/add")
                        .param("name", existing.getName())
                        .param("slug", "duplicate-name-slug"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories/add"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void testCategoryDetailDisplaysInformationAndAssociatedProducts() throws Exception {
        Category cat = categoryRepository.findAll().stream().findFirst().orElseThrow();

        var result = mockMvc.perform(get("/admin/categories/" + cat.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("activeMenu", "categories"))
                .andExpect(model().attributeExists("category"))
                .andExpect(model().attributeExists("products"))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertTrue(html.contains(cat.getName()), "Detail page must show category name");
    }

    @Test
    void testDeleteCategoryWithAssociatedProductsFailsAndProtectsData() throws Exception {
        // Tìm danh mục đang có sản phẩm
        Category catWithProducts = categoryRepository.findAll().stream()
                .filter(c -> productRepository.countByCategoryId(c.getId()) > 0)
                .findFirst().orElseThrow();

        Long catId = catWithProducts.getId();

        mockMvc.perform(post("/admin/categories/delete/" + catId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"))
                .andExpect(flash().attributeExists("errorMessage"));

        // Đảm bảo danh mục KHÔNG bị xóa
        assertTrue(categoryRepository.findById(catId).isPresent(), "Category with products must NOT be deleted");
    }

    @Test
    void testDeleteEmptyCategorySucceeds() throws Exception {
        // Tạo một danh mục mới không có sản phẩm nào
        Category emptyCat = new Category("Danh mục tạm " + System.currentTimeMillis(), "danh-muc-tam-" + System.currentTimeMillis(), null, "", true);
        emptyCat = categoryRepository.save(emptyCat);
        Long catId = emptyCat.getId();

        mockMvc.perform(post("/admin/categories/delete/" + catId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"))
                .andExpect(flash().attributeExists("successMessage"));

        // Đảm bảo danh mục đã được xóa thành công
        assertTrue(categoryRepository.findById(catId).isEmpty(), "Empty category must be deleted cleanly");
    }
}
