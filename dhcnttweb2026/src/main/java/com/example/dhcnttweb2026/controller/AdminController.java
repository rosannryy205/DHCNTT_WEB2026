package com.example.dhcnttweb2026.controller;

import com.example.dhcnttweb2026.model.Product;
import com.example.dhcnttweb2026.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {
    private final ProductService productService;

    public AdminController(ProductService productService) {
        this.productService = productService;
    }

    // TRANG ĐĂNG NHẬP ADMIN
    @GetMapping("/admin/login")
    public String showLoginPage(HttpSession session) {
        if (session.getAttribute("ADMIN_USER") != null) {
            return "redirect:/admin/dashboard";
        }
        return "admin/login";
    }

    @PostMapping("/admin/login")
    public String processLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {
        if ("admin".equalsIgnoreCase(username.trim()) && ("123456".equals(password) || "admin123".equals(password))) {
            session.setAttribute("ADMIN_USER", "Phan Tuấn Anh (Admin)");
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không chính xác (Mặc định: admin / 123456).");
        return "admin/login";
    }

    @GetMapping("/admin/logout")
    public String logout(HttpSession session, RedirectAttributes ra) {
        session.invalidate();
        ra.addFlashAttribute("logoutMessage", "Bạn đã đăng xuất khỏi hệ thống quản trị.");
        return "redirect:/admin/login";
    }

    // 1. TRANG DASHBOARD
    @GetMapping({"/", "/admin", "/admin/dashboard"})
    public String dashboard(Model model) {
        var all = productService.findAll(null, null);
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("pageTitle", "Dashboard Tổng Quan");
        model.addAttribute("subtitle", "Thống kê doanh thu, đơn hàng & tồn kho");
        model.addAttribute("totalRevenue", productService.getTotalRevenue());
        model.addAttribute("totalOrders", productService.getTotalOrdersCount());
        model.addAttribute("totalProducts", all.size());
        model.addAttribute("totalStock", productService.getTotalStock());
        model.addAttribute("featuredProduct", all.isEmpty() ? null : all.get(0));
        model.addAttribute("recentOrders", productService.getRecentOrders());
        return "admin/dashboard";
    }

    // 2. TRANG DANH SÁCH SẢN PHẨM
    @GetMapping("/admin/products")
    public String productList(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "ALL") String category,
            Model model) {
        var list = productService.findAll(keyword, category);
        model.addAttribute("activeMenu", "products");
        model.addAttribute("pageTitle", "Danh Sách Sản Phẩm");
        model.addAttribute("subtitle", list.size() + " thiết bị đang hiển thị");
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("products", list);
        return "admin/product-list";
    }

    // 3. TRANG THÊM SẢN PHẨM
    @GetMapping("/admin/products/add")
    public String showAddForm(Model model) {
        Product p = new Product();
        p.setCategory("iPhone");
        p.setStatus("Đang bán");
        model.addAttribute("activeMenu", "add");
        model.addAttribute("pageTitle", "Thêm Sản Phẩm Mới");
        model.addAttribute("subtitle", "Tạo mới thiết bị vào hệ thống");
        model.addAttribute("product", p);
        return "admin/product-add";
    }

    @PostMapping("/admin/products/add")
    public String addProduct(@ModelAttribute Product product, RedirectAttributes ra) {
        productService.save(product);
        ra.addFlashAttribute("successMessage", "Đã thêm sản phẩm mới: " + product.getName());
        return "redirect:/admin/products";
    }

    // 4. TRANG CHỈNH SỬA SẢN PHẨM
    @GetMapping("/admin/products/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        var all = productService.findAll(null, null);
        var opt = productService.findById(id);
        if (opt.isEmpty()) {
            if (!all.isEmpty()) return "redirect:/admin/products/edit/" + all.get(0).getId();
            return "redirect:/admin/products";
        }
        model.addAttribute("activeMenu", "edit");
        model.addAttribute("pageTitle", "Chỉnh Sửa Sản Phẩm");
        model.addAttribute("subtitle", "Mã SKU: " + opt.get().getSku());
        model.addAttribute("product", opt.get());
        model.addAttribute("allProducts", all);
        return "admin/product-edit";
    }

    @PostMapping("/admin/products/edit/{id}")
    public String updateProduct(@PathVariable Long id, @ModelAttribute Product product, RedirectAttributes ra) {
        product.setId(id);
        productService.save(product);
        ra.addFlashAttribute("successMessage", "Đã cập nhật sản phẩm: " + product.getName());
        return "redirect:/admin/products";
    }

    @PostMapping("/admin/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes ra) {
        productService.deleteById(id);
        ra.addFlashAttribute("successMessage", "Đã xóa sản phẩm khỏi kho.");
        return "redirect:/admin/products";
    }
}