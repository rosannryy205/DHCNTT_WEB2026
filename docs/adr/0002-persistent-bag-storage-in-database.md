# 0002. Lưu trữ Túi hàng (Bag) bền vững trong Cơ sở dữ liệu

**Trạng thái**: Đã lên kế hoạch (Phần Database Schema & JPA sẽ được triển khai ở Giai đoạn sau; Giai đoạn hiện tại tập trung hoàn thiện Giao diện UI/UX và Mock/Session Controller).

## Ngữ cảnh
Ban đầu, cơ chế lưu trữ Túi hàng (Bag) được cân nhắc sử dụng `HttpSession` tạm thời trên server hoặc `LocalStorage` trên trình duyệt. Tuy nhiên, khách hàng có nhu cầu bảo lưu trạng thái túi hàng xuyên suốt nhiều phiên duyệt web, không bị mất giỏ hàng khi đóng trình duyệt hoặc khi chuyển đổi giữa khách vãng lai (Guest) và tài khoản đã đăng nhập (Authenticated User).

## Quyết định
Chúng tôi quyết định lưu trữ dữ liệu Túi hàng trực tiếp trong Cơ sở dữ liệu (MySQL) thông qua mô hình:
- Bảng `bag`: Chứa định danh `id`, mã `guest_token` (UUID lưu trong Cookie cho khách vãng lai), và `user_id` (cho khách đã đăng nhập).
- Bảng `bag_item`: Liên kết với `bag` qua `bag_id`, chứa `sku` của `ProductVariant`, số lượng `quantity`, và thời gian tạo/cập nhật.
- Cơ chế hợp nhất (Merge Bag): Khi khách hàng vãng lai tiến hành đăng nhập, hệ thống sẽ tự động gộp các sản phẩm từ `guest_token` vào `bag` của tài khoản người dùng và hủy token vãng lai.

## Hệ quả
- Dữ liệu túi hàng được lưu trữ bền vững, không phụ thuộc vào vòng đời ngắn hạn của HTTP Session.
- Cần cơ chế dọn dẹp định kỳ (cron job/cleanup job) đối với các `bag` vãng lai bị bỏ quên (abandoned bags) sau một khoảng thời gian dài (ví dụ: 30 ngày) để tránh phình to database.
- Đòi hỏi tích hợp Spring Data JPA và quản lý Cookie an toàn (`HttpOnly`, `SameSite=Lax`).
