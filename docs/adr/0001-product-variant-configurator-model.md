# 0001. Mô hình Product Variant và Trình cấu hình Configurator động

## Ngữ cảnh
Dự án DH Store kinh doanh các dòng sản phẩm công nghệ theo chuẩn Apple (iPhone, MacBook, iPad, Watch...), trong đó mỗi dòng sản phẩm luôn có nhiều biến thể màu sắc (Color) và dung lượng lưu trữ (Storage) với mức giá và tồn kho riêng biệt. Nếu sử dụng mô hình sản phẩm phẳng đơn lẻ (Flat Product), dữ liệu sẽ bị nhân bản, URL bị phân mảnh và không thể dựng được trải nghiệm cấu hình Apple Configurator mượt mà.

## Quyết định
Chúng tôi quyết định thiết kế mô hình dữ liệu dạng **Product 1 - N ProductVariant**:
- `Product`: Đại diện cho dòng sản phẩm tổng thể (chứa tên, slug, thương hiệu, danh mục, thông số kỹ thuật chung, mô tả).
- `ProductVariant`: Đại diện cho một SKU cụ thể (chứa mã SKU, màu sắc kèm mã hex, dung lượng lưu trữ, giá bán, tồn kho, và danh sách ảnh riêng theo màu).
- Trang chi tiết sản phẩm (`/product/{slug}`) sẽ nhúng dữ liệu biến thể dưới dạng JSON model và sử dụng Client-side Dynamic Switching để chuyển đổi màu sắc, dung lượng, giá bán và hình ảnh tức thì mà không cần tải lại toàn bộ trang (No page reload).

## Hệ quả
- Khách hàng có trải nghiệm mua sắm mượt mà (60fps), đúng phong cách Apple Store.
- Việc quản lý kho (Inventory) và đơn hàng (Order Items) liên kết chính xác tới từng SKU cụ thể.
- Đòi hỏi logic đồng bộ giữa Product và Variant khi quản trị viên tạo/sửa sản phẩm trong Admin Console.
