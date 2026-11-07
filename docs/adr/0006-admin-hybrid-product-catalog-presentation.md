# 0006. Kiến trúc hiển thị Hybrid Danh mục Sản phẩm Quản trị (Admin Console)

> **Cập nhật 2026-11-07:** Quyết định bố cục Hybrid bên dưới đã được thay thế theo yêu cầu thiết kế lại. Trang quản lý dùng một bảng duy nhất, bỏ gallery và bốn thẻ KPI để ưu tiên tra cứu. Phần đầu giữ tiêu đề, số kết quả, tìm kiếm, lọc danh mục và icon thêm mới.
>
> Bảng gồm 7 cột: Sản phẩm (ảnh, tên, ID), SKU, Danh mục & hãng, Màu & bộ nhớ, Giá bán, Tồn kho, Trạng thái & thao tác. Nội dung ô không xuống dòng hoặc bị cắt; độ rộng cột theo nội dung, cuộn ngang trong vùng bảng khi thiếu chỗ. Mỗi dòng vẫn là một Product với thông tin cấu hình đại diện, không phải danh sách đầy đủ Product Variant hay tổng tồn kho.
>
> Các thao tác thêm, tìm kiếm, đặt lại, sửa và xóa dùng icon với tooltip, nhãn truy cập, vùng bấm 44×44px và focus bàn phím. Giữ palette Action Blue, nền trắng/parchment, không bóng đổ UI theo DESIGN.md; CSS giới hạn trong trang danh sách.
>
> Giữ luồng toast và modal xác nhận. Backend hiện xóa Product chưa có lịch sử đơn hàng, bao gồm các biến thể; Product có lịch sử đơn hàng được ngừng kinh doanh. Hộp xác nhận phải giải thích cả hai kết quả. Không thay đổi backend, thêm phân trang, sắp xếp hoặc thao tác hàng loạt trong lần thiết kế này.

## Quyết định ban đầu (được giữ để tham khảo)

## Ngữ cảnh
Trong phân hệ Admin Console của IUH Store, quản trị viên cần vừa có góc nhìn trực quan về hình ảnh nhận diện thương hiệu sản phẩm (theo phong cách triển lãm bảo tàng của Apple Store), vừa cần một công cụ kiểm toán tồn kho CSDL chính xác theo từng mã SKU, mức giá và trạng thái phân phối. Các giải pháp như chỉ dùng bảng thuần túy sẽ làm mất đi trải nghiệm nhiếp ảnh sản phẩm của Apple, trong khi dùng bộ chuyển chế độ (Segmented Switcher) phía client lại phát sinh trạng thái lưu tạm và gây gián đoạn mạch kiểm toán dữ liệu.

## Quyết định
Chúng tôi quyết định áp dụng mô hình **Hiển thị kép Hybrid với nhịp điệu luân phiên 3 canvas** trên [[product-list.html](file:///C:/Users/USER/orca/workspaces/DHCNTT_WEB2026/ch%E1%BB%89nh-s%E1%BB%ADa-giao-di%E1%BB%87n-product_list/dhcnttweb2026/src/main/resources/templates/admin/product-list.html)]:
1. **Section 1 (`tile-light` - #ffffff)**: Tiêu đề Display, 4 thẻ chỉ số nhanh KPI (Tổng thiết bị, danh mục, tồn kho CSDL, bộ lọc) và thanh công cụ tìm kiếm Pill 44px + dải chip danh mục `configurator-option-chip`.
2. **Section 2 (`tile-parchment` - #f5f5f7)**: Thư viện thẻ trưng bày `store-utility-card` với hình ảnh sản phẩm trong suốt mang bóng đổ Signature Drop-shadow (`3px 5px 30px rgba(0,0,0,0.22)`), thông tin SKU, cấu hình và liên kết chỉnh sửa rõ ràng.
3. **Section 3 (`tile-light` - #ffffff)**: Bảng thông số kỹ thuật chi tiết `spec-table` trong khung viền mềm 18px với ảnh thu nhỏ thumbnail 44×44px, phân loại, giá niêm yết, số lượng tồn kho và các nút thao tác tròn 44×44px đạt chuẩn vùng chạm cảm ứng.
4. **Cơ chế phản hồi & An toàn dữ liệu**: Sử dụng độc quyền popup thông báo Apple HUD Floating Toast qua `apple-notification.js` (không dùng banner tĩnh inline chiếm diện tích). Khi xóa, tích hợp modal xác nhận `data-apple-confirm` và ưu tiên chuyển trạng thái sang `is_active = false` (Ngừng kinh doanh) để bảo toàn dữ liệu đơn hàng lịch sử.

## Hệ quả
- Giao diện tuân thủ 100% tài liệu [[docs/DESIGN.md](file:///C:/Users/USER/orca/workspaces/DHCNTT_WEB2026/ch%E1%BB%89nh-s%E1%BB%ADa-giao-di%E1%BB%87n-product_list/docs/DESIGN.md)] về nhịp điệu canvas, màu Action Blue duy nhất, quy tắc bóng đổ độc bản và thang font chuẩn.
- Quản trị viên nắm bắt nhanh chóng cả diện mạo hình ảnh lẫn số liệu CSDL chi tiết trong một luồng cuộn trang tự nhiên.
- Đòi hỏi duy trì cấu trúc DTO `AdminProductDto` đồng bộ phẳng giữa bảng `products` và `product_variants`.
