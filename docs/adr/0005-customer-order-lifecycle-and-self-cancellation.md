# 0005. Quản lý Hồ sơ Khách hàng, Theo dõi Vòng đời Đơn hàng và Cơ chế Tự hủy Đơn

**Trạng thái**: Đã chấp thuận

## Ngữ cảnh
Sau khi hoàn thiện quy trình thanh toán và đặt hàng (`/checkout`), hệ thống IUH Store cần cung cấp không gian cá nhân cho Customer để quản lý thông tin tài khoản và theo dõi trạng thái đơn hàng đã đặt. Trước đây:
- Chưa có giao diện Hồ sơ khách hàng (`Customer Profile`) cho phép cập nhật thông tin nhận hàng và đổi mật khẩu an toàn.
- Chưa có giao diện Lịch sử đơn hàng (`Order History`) trên Storefront; menu dropdown tài khoản trên Global Nav vẫn để nhãn "Đơn hàng của tôi (Sắp ra mắt)".
- Khách hàng không thể tự tra cứu tiến trình đơn hàng (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED) hoặc tự hủy đơn hàng chưa thanh toán khi thay đổi ý định, dẫn đến áp lực hỗ trợ khách hàng thủ công cho đội ngũ Admin/Staff.

## Quyết định
Chúng tôi quyết định:
1. **Phân định rõ ràng cấu trúc URL phân hệ Customer**:
   - `/profile`: Quản lý thông tin cá nhân (Họ tên, Số điện thoại, Địa chỉ giao hàng mặc định) và Đổi mật khẩu. Khóa trường Email ở chế độ chỉ đọc (Read-only) vì là định danh tài khoản duy nhất.
   - `/orders`: Danh sách thẻ đơn hàng (`Order Card`) với bộ lọc trạng thái (Tất cả, Đang xử lý, Đang giao, Đã giao, Đã hủy) và trạng thái rỗng (Empty State) chuẩn Apple.
   - `/orders/{orderCode}`: Chi tiết đơn hàng với thanh tiến trình trực quan Apple Order Status Stepper (4 bước), thông tin người nhận, danh sách sản phẩm, và tóm tắt thanh toán.
2. **Cơ chế Tự phục vụ Hủy đơn hàng (Self-Service Cancellation)**:
   - Cho phép Customer tự hủy đơn hàng trực tiếp trên giao diện khi đơn thỏa mãn điều kiện `Order.canCancel()` (trạng thái `Pending` hoặc `Processing`, và phương thức thanh toán COD với `paymentStatus == PENDING`).
   - Yêu cầu xác nhận qua Modal kèm lý do hủy có cấu trúc, lưu trữ vào `cancellationReason`.
   - Kích hoạt cơ chế hoàn trả tồn kho nguyên tử (atomic inventory rollback) cho từng `ProductVariant` trong đơn để bảo toàn tính nhất quán của kho hàng.
   - Khóa thao tác hủy khi đơn hàng đã chuyển sang `Shipped`/`Delivered` hoặc đã thanh toán trực tuyến thành công.
3. **Phân quyền và Bảo mật nghiêm ngặt (IDOR Prevention)**:
   - Toàn bộ các route `/profile/**` và `/orders/**` được bảo vệ bởi `CustomerAuthInterceptor`.
   - Bắt buộc kiểm tra quyền sở hữu `order.getUser().getId().equals(currentUser.getId())` trước khi hiển thị chi tiết hoặc tiếp nhận yêu cầu hủy. Nếu vi phạm, trả về lỗi HTTP 404 Not Found để ngăn chặn việc dò quét mã đơn hàng trái phép.
4. **Tối ưu hóa Truy vấn và DTO Projection**:
   - Sử dụng các DTO chuyên biệt (`CustomerOrderSummaryDto`, `CustomerOrderDetailDto`, `CustomerProfileDto`) kết hợp `JOIN FETCH` trong Spring Data JPA để truy xuất toàn bộ dữ liệu trong một truy vấn, triệt tiêu lỗi N+1 Query và `LazyInitializationException`.
5. **Đồng bộ Luồng Trải nghiệm (End-to-End Navigation)**:
   - Kích hoạt liên kết chính thức trong Global Nav menu (`/profile`, `/orders`).
   - Bổ sung nút CTA "Theo dõi đơn hàng" trên màn hình đặt hàng thành công (`/checkout-success`).

## Hệ quả
- Khách hàng có trải nghiệm tự phục vụ liền mạch, trực quan và an toàn từ lúc đặt hàng đến khi nhận hàng hoặc hủy đơn.
- Giảm tải đáng kể cho bộ phận vận hành nhờ quy trình tự hủy đơn kèm hoàn kho tự động và kiểm soát chuyển trạng thái chặt chẽ.
- Bảo mật thông tin đơn hàng và tài khoản khách hàng ở mức cao nhất, ngăn ngừa hoàn toàn rủi ro IDOR.
- Đánh đổi: Cần duy trì tính đồng bộ của session đăng nhập khi cập nhật hồ sơ và đảm bảo concurrency lock (Pessimistic Write) khi thực hiện hủy đơn để tránh race condition về tồn kho.
