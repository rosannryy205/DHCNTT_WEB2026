# DH Store

Hệ thống thương mại điện tử chuyên kinh doanh sản phẩm Apple và thiết bị công nghệ cao cấp, bao gồm hai phân hệ Storefront (Khách hàng) và Admin Console (Quản trị).

## Language

**Product**:
Một dòng sản phẩm tổng thể (ví dụ: iPhone 16 Pro, MacBook Pro 14", Apple Watch Ultra 2).
_Avoid_: Hàng hóa, vật phẩm, item

**Product Variant**:
Phiên bản cấu hình cụ thể có thể đặt mua của một Product, xác định bởi tổ hợp các thuộc tính (Màu sắc, Dung lượng bộ nhớ, RAM...), có SKU, giá bán, tồn kho và bộ ảnh riêng.
_Avoid_: Sản phẩm con, cấu hình phụ, sub-product

**SKU (Stock Keeping Unit)**:
Mã định danh duy nhất của từng Product Variant dùng để quản lý kho và xử lý đơn hàng.
_Avoid_: Mã sản phẩm, mã vạch, barcode

**Option / Attribute**:
Thuộc tính cấu hình lựa chọn của sản phẩm (ví dụ: Màu sắc - Finish/Color, Dung lượng - Storage, Kích cỡ màn hình - Size).
_Avoid_: Tính năng, thông số, property

**Bag (Túi hàng)**:
Nơi lưu trữ tạm thời các Product Variant mà khách hàng dự định mua trước khi thanh toán (chuẩn thuật ngữ Apple).
_Avoid_: Giỏ hàng, cart, giỏ mua hàng

**Bag Item (Dòng sản phẩm trong túi)**:
Một bản ghi trong Bag liên kết với một Product Variant cụ thể kèm theo số lượng đặt mua và đơn giá tại thời điểm thêm vào túi.
_Avoid_: Cart item, món hàng, dòng hàng

**Guest Token**:
Mã định danh duy nhất (UUID) lưu trong cookie của trình duyệt để gắn kết Túi hàng trong cơ sở dữ liệu với khách hàng chưa đăng nhập.
_Avoid_: Session token, visitor id, cookie id

**Order Summary (Tóm tắt đơn hàng)**:
Bảng tổng hợp chi phí trong Bag và Checkout bao gồm tạm tính, phí vận chuyển dự kiến, giảm giá (nếu có) và tổng số tiền thanh toán cuối cùng.
_Avoid_: Hóa đơn tạm, bảng tính tiền, cart summary

**Tech Specs (Thông số kỹ thuật)**:
Bảng chi tiết thông số phần cứng của sản phẩm (vi xử lý chip, kích thước màn hình, độ phân giải camera, chuẩn pin, cổng kết nối).
_Avoid_: Cấu hình máy, thông tin máy, description

**Compatible Accessories (Phụ kiện tương thích)**:
Các vật phẩm phụ kiện đi kèm có thể mua thêm để sử dụng cùng sản phẩm chính (ví dụ: ốp lưng MagSafe, củ sạc nhanh 30W, tai nghe AirPods).
_Avoid_: Sản phẩm mua kèm, đồ đi kèm

**Related Products (Sản phẩm liên quan)**:
Các dòng sản phẩm cùng danh mục hoặc cùng phân khúc người dùng có thể cân nhắc thay thế hoặc khám phá thêm (ví dụ: đang xem iPhone 16 Pro thì gợi ý iPhone 16 thường, iPhone 16 Plus hoặc iPhone 15 Pro).
_Avoid_: Sản phẩm tương tự, hàng tương tự
