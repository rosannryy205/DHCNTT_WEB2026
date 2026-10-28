package com.example.dhcnttweb2026.controller;

import com.example.dhcnttweb2026.entity.Product;
import com.example.dhcnttweb2026.model.AdminCategoryDto;
import com.example.dhcnttweb2026.model.AdminProductDto;
import com.example.dhcnttweb2026.repository.BrandRepository;
import com.example.dhcnttweb2026.repository.ProductRepository;
import com.example.dhcnttweb2026.service.CategoryService;
import com.example.dhcnttweb2026.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@SuppressWarnings("null")
public class AdminController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    public AdminController(ProductService productService,
                           CategoryService categoryService,
                           BrandRepository brandRepository,
                           ProductRepository productRepository) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.brandRepository = brandRepository;
        this.productRepository = productRepository;
    }

    // =========================================================================
    // 1. TRANG DASHBOARD TỔNG QUAN
    // =========================================================================
    @GetMapping({"/admin", "/admin/", "/admin/dashboard"})
    public String dashboard(Model model) {
        var all = productService.findAll(null, null);
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("pageTitle", "Dashboard Tổng Quan");
        model.addAttribute("subtitle", "Thống kê doanh thu, đơn hàng & tồn kho từ CSDL");
        model.addAttribute("totalRevenue", productService.getTotalRevenue());
        model.addAttribute("totalOrders", productService.getTotalOrdersCount());
        model.addAttribute("totalProducts", all.size());
        model.addAttribute("totalStock", productService.getTotalStock());
        model.addAttribute("featuredProduct", all.isEmpty() ? null : all.get(0));
        model.addAttribute("recentOrders", productService.getRecentOrders());
        return "admin/dashboard";
    }

    // =========================================================================
    // 2. QUẢN LÝ SẢN PHẨM (PRODUCTS)
    // =========================================================================

    // Danh sách sản phẩm
    @GetMapping({"/admin/products", "/admin/products/"})
    public String productList(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "ALL") String category,
            Model model) {
        var list = productService.findAll(keyword, category);
        var categories = categoryService.findAllActive();

        model.addAttribute("activeMenu", "products");
        model.addAttribute("pageTitle", "Danh Sách Sản Phẩm");
        model.addAttribute("subtitle", list.size() + " thiết bị đang hiển thị trong CSDL");
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("products", list);
        model.addAttribute("categoryOptions", categories);
        return "admin/product-list";
    }

    // Giao diện Thêm sản phẩm mới
    @GetMapping("/admin/products/add")
    public String showAddProductForm(Model model) {
        AdminProductDto p = new AdminProductDto();
        var categories = categoryService.findAllActive();
        if (!categories.isEmpty()) {
            p.setCategoryId(categories.get(0).getId());
            p.setCategory(categories.get(0).getName());
        } else {
            p.setCategory("iPhone");
        }
        p.setBrand("Apple");
        p.setStatus("Đang bán");
        p.setColor("Titan Tự Nhiên");
        p.setStorage("256GB");
        p.setRam("12GB");
        p.setStock(20);

        model.addAttribute("activeMenu", "products");
        model.addAttribute("pageTitle", "Thêm Sản Phẩm Mới");
        model.addAttribute("subtitle", "Tạo mới thiết bị và cấu hình vào CSDL");
        model.addAttribute("product", p);
        model.addAttribute("categories", categories);
        model.addAttribute("brands", brandRepository.findAllByOrderByNameAsc());
        return "admin/product-add";
    }

    // Xử lý Lưu Thêm sản phẩm mới
    @PostMapping("/admin/products/add")
    public String addProduct(@ModelAttribute AdminProductDto product, RedirectAttributes ra) {
        try {
            productService.save(product);
            ra.addFlashAttribute("successMessage", "Đã thêm thành công sản phẩm mới: " + product.getName());
            return "redirect:/admin/products";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("product", product);
            return "redirect:/admin/products/add";
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", "Lỗi khi lưu sản phẩm vào CSDL: " + ex.getMessage());
            return "redirect:/admin/products/add";
        }
    }

    // Giao diện Chỉnh sửa sản phẩm
    @GetMapping("/admin/products/edit/{id}")
    public String showEditProductForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        var opt = productService.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm có ID: " + id);
            return "redirect:/admin/products";
        }

        var all = productService.findAll(null, null);
        var categories = categoryService.findAllActive();
        var brands = brandRepository.findAllByOrderByNameAsc();

        model.addAttribute("activeMenu", "products");
        model.addAttribute("pageTitle", "Chỉnh Sửa Sản Phẩm");
        model.addAttribute("subtitle", "Mã SKU: " + opt.get().getSku());
        model.addAttribute("product", opt.get());
        model.addAttribute("allProducts", all);
        model.addAttribute("categories", categories);
        model.addAttribute("brands", brands);
        return "admin/product-edit";
    }

    // Xử lý Cập nhật sản phẩm
    @PostMapping("/admin/products/edit/{id}")
    public String updateProduct(@PathVariable Long id, @ModelAttribute AdminProductDto product, RedirectAttributes ra) {
        try {
            product.setId(id);
            productService.save(product);
            ra.addFlashAttribute("successMessage", "Đã cập nhật thành công sản phẩm: " + product.getName());
            return "redirect:/admin/products";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/products/edit/" + id;
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", "Lỗi cập nhật sản phẩm: " + ex.getMessage());
            return "redirect:/admin/products/edit/" + id;
        }
    }

    // Xử lý Xóa sản phẩm
    @PostMapping("/admin/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes ra) {
        try {
            String msg = productService.deleteById(id);
            ra.addFlashAttribute("successMessage", msg);
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", "Không thể xóa sản phẩm: " + ex.getMessage());
        }
        return "redirect:/admin/products";
    }

    // =========================================================================
    // 3. QUẢN LÝ DANH MỤC (CATEGORIES)
    // =========================================================================

    // Danh sách danh mục
    @GetMapping({"/admin/categories", "/admin/categories/"})
    public String categoryList(@RequestParam(required = false, defaultValue = "") String keyword, Model model) {
        var categories = categoryService.findAll(keyword);
        model.addAttribute("activeMenu", "categories");
        model.addAttribute("pageTitle", "Quản Lý Danh Mục");
        model.addAttribute("subtitle", categories.size() + " danh mục trong CSDL");
        model.addAttribute("keyword", keyword);
        model.addAttribute("categories", categories);
        return "admin/category-list";
    }

    // Giao diện Thêm danh mục mới
    @GetMapping("/admin/categories/add")
    public String showAddCategoryForm(Model model) {
        AdminCategoryDto c = new AdminCategoryDto();
        c.setIsActive(true);

        model.addAttribute("activeMenu", "categories");
        model.addAttribute("pageTitle", "Thêm Danh Mục Mới");
        model.addAttribute("subtitle", "Tạo mới danh mục thiết bị Apple");
        model.addAttribute("category", c);
        model.addAttribute("rootCategories", categoryService.findAllRootCategories());
        return "admin/category-add";
    }

    // Xử lý Lưu Thêm danh mục mới
    @PostMapping("/admin/categories/add")
    public String addCategory(@ModelAttribute AdminCategoryDto category, RedirectAttributes ra) {
        try {
            categoryService.save(category);
            ra.addFlashAttribute("successMessage", "Đã thêm thành công danh mục mới: " + category.getName());
            return "redirect:/admin/categories";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            ra.addFlashAttribute("category", category);
            return "redirect:/admin/categories/add";
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", "Lỗi khi lưu danh mục: " + ex.getMessage());
            return "redirect:/admin/categories/add";
        }
    }

    // Giao diện Chỉnh sửa danh mục
    @GetMapping("/admin/categories/edit/{id}")
    public String showEditCategoryForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        var opt = categoryService.findDtoById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy danh mục có ID: " + id);
            return "redirect:/admin/categories";
        }

        // Lấy danh sách danh mục cha tiềm năng (loại trừ chính danh mục đang sửa)
        var rootCategories = categoryService.findAllRootCategories().stream()
                .filter(c -> !c.getId().equals(id))
                .toList();

        model.addAttribute("activeMenu", "categories");
        model.addAttribute("pageTitle", "Chỉnh Sửa Danh Mục");
        model.addAttribute("subtitle", "ID: " + id + " — " + opt.get().getName());
        model.addAttribute("category", opt.get());
        model.addAttribute("rootCategories", rootCategories);
        return "admin/category-edit";
    }

    // Xử lý Cập nhật danh mục
    @PostMapping("/admin/categories/edit/{id}")
    public String updateCategory(@PathVariable Long id, @ModelAttribute AdminCategoryDto category, RedirectAttributes ra) {
        try {
            category.setId(id);
            categoryService.save(category);
            ra.addFlashAttribute("successMessage", "Đã cập nhật thành công danh mục: " + category.getName());
            return "redirect:/admin/categories";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/categories/edit/" + id;
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", "Lỗi cập nhật danh mục: " + ex.getMessage());
            return "redirect:/admin/categories/edit/" + id;
        }
    }

    // Xem chi tiết danh mục và danh sách sản phẩm thuộc danh mục
    @GetMapping("/admin/categories/{id}")
    public String showCategoryDetail(@PathVariable Long id, Model model, RedirectAttributes ra) {
        var opt = categoryService.findDtoById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy danh mục có ID: " + id);
            return "redirect:/admin/categories";
        }

        List<Product> products = productRepository.findByCategoryId(id);

        model.addAttribute("activeMenu", "categories");
        model.addAttribute("pageTitle", "Chi Tiết Danh Mục: " + opt.get().getName());
        model.addAttribute("subtitle", products.size() + " sản phẩm đang thuộc danh mục này");
        model.addAttribute("category", opt.get());
        model.addAttribute("products", products);
        return "admin/category-detail";
    }

    // Xử lý Xóa danh mục khi thỏa mãn các ràng buộc khóa ngoại
    @PostMapping("/admin/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes ra) {
        try {
            categoryService.deleteById(id);
            ra.addFlashAttribute("successMessage", "Đã xóa danh mục thành công.");
        } catch (IllegalStateException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", "Không thể xóa danh mục: " + ex.getMessage());
        }
        return "redirect:/admin/categories";
    }
}
