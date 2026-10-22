# 0003. Kết xuất phân mảnh Server-driven (Thymeleaf Fragment) cho Bộ lọc Danh mục Sản phẩm

## Ngữ cảnh
Khi xây dựng trang duyệt danh mục sản phẩm (`/products`) với tính năng lọc đa tiêu chí (danh mục, mức giá, màu sắc, dung lượng, sắp xếp), chúng tôi cần một cơ chế cập nhật kết quả lọc tức thì mà không phải tải lại toàn bộ trang (no full-page reload).
Có hai phương án kỹ thuật phổ biến:
1. Xây dựng REST API trả về JSON, sau đó viết mã JavaScript ở client để lặp mảng và ghép chuỗi HTML (hoặc dùng template engine client).
2. Tận dụng kiến trúc kết xuất phía máy chủ (Server-Side Rendering) với Thymeleaf Fragment: Controller xử lý lọc dữ liệu và trả về trực tiếp đoạn HTML của lưới sản phẩm (`product-grid`). Client JavaScript chỉ gửi Fetch request và thay thế vùng DOM (`innerHTML`).

## Quyết định
Chúng tôi quyết định áp dụng mô hình **Server-Driven UI thông qua Thymeleaf Fragment kết hợp Fetch API và đồng bộ URL `history.pushState`**:
- Controller tại `/products/filter` sẽ nhận các tham số lọc, truy vấn/lọc danh sách `Product`, và trả về view fragment `"client/fragments/product-grid :: product-grid"`.
- Tránh trùng lặp logic hiển thị (DRY): Toàn bộ cấu trúc thẻ HTML, class CSS Apple design token, logic định dạng tiền tệ Việt Nam đồng (`formatVND`), nhãn khuyến mãi, và các chấm màu sắc chỉ định nghĩa duy nhất tại một file template Thymeleaf.
- JavaScript ở phía client đóng vai trò "mỏng" (thin client): lắng nghe sự kiện thay đổi của bộ lọc, thu thập tham số URL query, kích hoạt hiệu ứng mờ chuyển cảnh (opacity transition), gửi request bất đồng bộ và cập nhật URL trình duyệt để hỗ trợ bookmark / copy link.

## Hệ quả
- Loại bỏ hoàn toàn sự sai lệch giao diện giữa lần tải trang đầu tiên (SSR) và các lần lọc tiếp theo (CSR).
- Tối ưu hiệu năng phát triển và kích thước gói tải xuống (không cần thư viện template nặng ở frontend).
- Trải nghiệm người dùng đạt tốc độ phản hồi cao (60fps) và giữ nguyên khả năng tương thích SEO cũng như điều hướng lịch sử trình duyệt (Back/Forward).
