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
Nơi lưu trữ tạm thời các Product Variant mà khách hàng dự định mua trước khi thanh toán (chuẩn thuật ngữ Apple; trong database và JPA entity được ánh xạ kỹ thuật dưới tên `Cart` / `cart_items`).
_Avoid_: Giỏ hàng, giỏ mua hàng

**Bag Item (Dòng sản phẩm trong túi)**:
Một bản ghi trong Bag liên kết với một Product Variant cụ thể kèm theo số lượng đặt mua và đơn giá tại thời điểm thêm vào túi (trong database và JPA entity được ánh xạ kỹ thuật là `CartItem`).
_Avoid_: Món hàng, dòng hàng

**Payment Status (Trạng thái thanh toán)**:
Tình trạng xử lý dòng tiền của một đơn hàng, gồm 4 trạng thái chuẩn: PENDING (chờ thanh toán), PAID (đã thanh toán thành công), FAILED (thanh toán thất bại), và REFUNDED (đã hoàn tiền).
_Avoid_: Tình trạng trả tiền, trạng thái ví

**Order Status (Trạng thái đơn hàng)**:
Vòng đời xử lý đơn hàng từ lúc đặt đến lúc hoàn tất, gồm: PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED.
_Avoid_: Tình trạng giao, tiến độ đơn

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

**Product Card (Thẻ sản phẩm trên danh mục)**:
Khối giao diện trực quan đại diện cho một Product tổng thể trên trang danh sách, hiển thị ảnh mẫu, tên dòng máy, mức giá khởi điểm ("Từ ..."), các chấm tùy chọn màu sắc (Finish Swatches) và liên kết chuyển đến trang chi tiết.
_Avoid_: Hộp sản phẩm, item card, ô hàng hóa

**Filter Facet (Nhóm tiêu chí lọc)**:
Một chiều phân loại thuộc tính của sản phẩm dùng để thu hẹp kết quả tìm kiếm (ví dụ: Danh mục, Khoảng giá, Dung lượng lưu trữ, Màu sắc, Tình trạng tồn kho).
_Avoid_: Tiêu chí tìm, bộ lọc con, filter group

**Active Filter Chip (Thẻ lọc đang kích hoạt)**:
Thẻ huy hiệu đại diện cho một tiêu chí lọc cụ thể đang được áp dụng, cho phép người dùng click để gỡ bỏ nhanh tiêu chí đó hoặc xóa toàn bộ bộ lọc.
_Avoid_: Tag lọc, nhãn đang lọc, filter badge

**Sort Criterion (Tiêu chí sắp xếp)**:
Quy tắc thứ tự hiển thị danh sách sản phẩm trên lưới (ví dụ: Giá tăng dần, Giá giảm dần, Mới nhất, Bán chạy).
_Avoid_: Kiểu xếp, thứ tự lọc

**Admin Product Management (Quản lý sản phẩm)**:
Không gian làm việc tập trung trong Admin Console cho phép quản trị viên xem danh mục sản phẩm, tìm kiếm, lọc theo thiết bị, và kích hoạt các hành động theo ngữ cảnh (Thêm sản phẩm mới qua nút CTA hoặc Chỉnh sửa cấu hình từng sản phẩm).
_Avoid_: Mục Thêm/Sửa đặt rời rạc trên sidebar điều hướng cấp cao.

**Customer**:
Người dùng mua sắm trên Storefront, sở hữu túi hàng và lịch sử đơn hàng cá nhân.
_Avoid_: Client, buyer, end-user

**Staff**:
Nhân viên vận hành hệ thống cửa hàng, có quyền truy cập vào phân hệ Admin Console để xử lý đơn hàng và kiểm tra kho thiết bị.
_Avoid_: Employee, clerk, moderator

**Admin**:
Quản trị viên cấp cao nhất của hệ thống DH Store, có toàn quyền cấu hình, quản trị sản phẩm, phân quyền và giám sát hoạt động.
_Avoid_: Superuser, root, boss
