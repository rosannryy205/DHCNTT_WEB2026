# 0004. Bắt buộc Đăng nhập Customer để Mua hàng và Loại bỏ Khách vãng lai (Guest)

**Trạng thái**: Đã chấp thuận (Thay thế một phần cơ chế Guest của ADR-0002)

## Ngữ cảnh
Trước đây, ADR-0002 dự kiến cho phép khách vãng lai (Guest) thêm sản phẩm vào Túi hàng thông qua `guest_token` trong cookie và tiến hành hợp nhất (Merge Bag) khi đăng nhập. Tuy nhiên, điều này làm tăng độ phức tạp trong việc duy trì session vãng lai, xử lý xung đột số lượng khi gộp giỏ, và sinh ra nhiều giỏ hàng rác (abandoned bags) trong cơ sở dữ liệu.

## Quyết định
Chúng tôi quyết định:
1. **Loại bỏ hoàn toàn khái niệm khách vãng lai (Guest / Guest Token) trong luồng mua sắm**: Người dùng chưa đăng nhập chỉ được phép xem danh mục và chi tiết sản phẩm.
2. **Bắt buộc đăng nhập Customer để sử dụng Túi hàng**: Khi người dùng chưa đăng nhập bấm "Thêm vào túi" (Add to Bag) hoặc truy cập vào trang `/bag`, hệ thống sẽ chặn lại và tự động chuyển hướng sang trang `/login?returnUrl=...`.
3. **Mỗi Túi hàng (`Cart`) liên kết trực tiếp 1-1 với một tài khoản Customer (`user_id` NOT NULL)**: Loại bỏ logic và cột `guest_token`, loại bỏ hoàn toàn cơ chế `Merge Bag`.
4. Sau khi đăng nhập thành công, hệ thống chuyển hướng Customer về đúng trang sản phẩm hoặc trang túi hàng theo `returnUrl`.

## Hệ quả
- Đơn giản hóa kiến trúc mã nguồn và cơ sở dữ liệu: không cần sinh/quản lý UUID `guest_token` trong Cookie, không cần logic hợp nhất giỏ hàng phức tạp, không cần cron job dọn dẹp giỏ hàng vãng lai.
- Dữ liệu `Cart` và `CartItem` luôn gắn chặt với người dùng xác thực (`users.id`).
- Đánh đổi: Khách hàng chưa có tài khoản phải đăng ký/đăng nhập trước khi có thể bắt đầu chọn mua sản phẩm.
