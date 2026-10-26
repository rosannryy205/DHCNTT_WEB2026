-- ==============================================================================
-- FILE DỮ LIỆU MẪU (SEED DATA) CHO CƠ SỞ DỮ LIỆU DH STORE (dhcntt_web2026)
-- Thoả mãn đầy đủ cấu trúc bảng, kiểu dữ liệu, quan hệ khoá ngoại (Foreign Keys)
-- và các ràng buộc ENUM, NOT NULL, UNIQUE, BIT(1) trong database.
-- ==============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Xoá dữ liệu cũ theo thứ tự an toàn (nếu cần làm mới dữ liệu)
DELETE FROM `order_items`;
DELETE FROM `orders`;
DELETE FROM `cart_items`;
DELETE FROM `carts`;
DELETE FROM `product_variants`;
DELETE FROM `products`;
DELETE FROM `category_brand`;
DELETE FROM `categories`;
DELETE FROM `brands`;
DELETE FROM `users`;

-- Reset AUTO_INCREMENT về 1
ALTER TABLE `order_items` AUTO_INCREMENT = 1;
ALTER TABLE `orders` AUTO_INCREMENT = 1;
ALTER TABLE `cart_items` AUTO_INCREMENT = 1;
ALTER TABLE `carts` AUTO_INCREMENT = 1;
ALTER TABLE `product_variants` AUTO_INCREMENT = 1;
ALTER TABLE `products` AUTO_INCREMENT = 1;
ALTER TABLE `category_brand` AUTO_INCREMENT = 1;
ALTER TABLE `categories` AUTO_INCREMENT = 1;
ALTER TABLE `brands` AUTO_INCREMENT = 1;
ALTER TABLE `users` AUTO_INCREMENT = 1;

-- ==============================================================================
-- 1. BẢNG `brands` (Thương hiệu)
-- Các cột: id, name, slug, logo, description, created_at, updated_at
-- ==============================================================================
INSERT INTO `brands` (`id`, `name`, `slug`, `logo`, `description`, `created_at`, `updated_at`) VALUES
(1, 'Apple', 'apple', 'https://upload.wikimedia.org/wikipedia/commons/f/fa/Apple_logo_black.svg', 'Tập đoàn công nghệ hàng đầu thế giới với hệ sinh thái iPhone, Mac, iPad, Watch, AirPods.', '2026-10-01 08:00:00.000000', '2026-10-01 08:00:00.000000'),
(2, 'Beats', 'beats', 'https://upload.wikimedia.org/wikipedia/commons/1/17/Beats_Electronics_logo.svg', 'Thương hiệu âm thanh cao cấp thuộc Apple với các dòng tai nghe Beats Studio, Beats Solo, Beats Fit Pro.', '2026-10-01 08:00:00.000000', '2026-10-01 08:00:00.000000');

-- ==============================================================================
-- 2. BẢNG `categories` (Danh mục sản phẩm)
-- Các cột: id, name, slug, image, is_active, parent_id, created_at, updated_at
-- Danh mục gốc có parent_id = NULL; danh mục con có parent_id trỏ về danh mục cha
-- ==============================================================================
INSERT INTO `categories` (`id`, `name`, `slug`, `image`, `is_active`, `parent_id`, `created_at`, `updated_at`) VALUES
-- Danh mục gốc (Root Categories)
(1, 'iPhone', 'iphone', 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-model-unselect-gallery-2-202409?wid=800&hei=600&fmt=jpeg&qlt=90', 1, NULL, '2026-10-01 08:00:00.000000', '2026-10-01 08:00:00.000000'),
(2, 'Mac', 'mac', 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/mbp14-spaceblack-select-202310?wid=800&hei=600&fmt=jpeg&qlt=90', 1, NULL, '2026-10-01 08:00:00.000000', '2026-10-01 08:00:00.000000'),
(3, 'iPad', 'ipad', 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/ipad-pro-finish-unselect-gallery-1-202405?wid=800&hei=600&fmt=jpeg&qlt=90', 1, NULL, '2026-10-01 08:00:00.000000', '2026-10-01 08:00:00.000000'),
(4, 'Apple Watch', 'watch', 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/ultra-case-unselect-gallery-1-202409?wid=800&hei=600&fmt=jpeg&qlt=90', 1, NULL, '2026-10-01 08:00:00.000000', '2026-10-01 08:00:00.000000'),
(5, 'AirPods', 'airpods', 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/airpods-pro-2-hero-select-202409?wid=800&hei=600&fmt=jpeg&qlt=90', 1, NULL, '2026-10-01 08:00:00.000000', '2026-10-01 08:00:00.000000'),
(6, 'Phụ Kiện', 'phu-kien', 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/MU7T2_GEO_VN?wid=800&hei=600&fmt=jpeg&qlt=90', 1, NULL, '2026-10-01 08:00:00.000000', '2026-10-01 08:00:00.000000'),

-- Danh mục con (Sub Categories)
(7, 'iPhone 16 Series', 'iphone-16-series', 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-model-unselect-gallery-2-202409?wid=800&hei=600&fmt=jpeg&qlt=90', 1, 1, '2026-10-01 08:00:00.000000', '2026-10-01 08:00:00.000000'),
(8, 'iPhone 15 Series', 'iphone-15-series', 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-15-finish-select-202309-6-1inch-blue?wid=800&hei=600&fmt=jpeg&qlt=90', 1, 1, '2026-10-01 08:00:00.000000', '2026-10-01 08:00:00.000000'),
(9, 'MacBook Pro', 'macbook-pro', 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/mbp14-spaceblack-select-202310?wid=800&hei=600&fmt=jpeg&qlt=90', 1, 2, '2026-10-01 08:00:00.000000', '2026-10-01 08:00:00.000000'),
(10, 'MacBook Air', 'macbook-air', 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/mba13-midnight-select-202402?wid=800&hei=600&fmt=jpeg&qlt=90', 1, 2, '2026-10-01 08:00:00.000000', '2026-10-01 08:00:00.000000'),
(11, 'iPad Pro', 'ipad-pro', 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/ipad-pro-finish-unselect-gallery-1-202405?wid=800&hei=600&fmt=jpeg&qlt=90', 1, 3, '2026-10-01 08:00:00.000000', '2026-10-01 08:00:00.000000'),
(12, 'iPad Air', 'ipad-air', 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/ipad-air-finish-unselect-gallery-1-202405?wid=800&hei=600&fmt=jpeg&qlt=90', 1, 3, '2026-10-01 08:00:00.000000', '2026-10-01 08:00:00.000000');

-- ==============================================================================
-- 3. BẢNG `category_brand` (Liên kết Danh mục & Thương hiệu)
-- Các cột: id, brand_id, category_id
-- ==============================================================================
INSERT INTO `category_brand` (`id`, `brand_id`, `category_id`) VALUES
(1, 1, 1),   -- Apple - iPhone
(2, 1, 2),   -- Apple - Mac
(3, 1, 3),   -- Apple - iPad
(4, 1, 4),   -- Apple - Watch
(5, 1, 5),   -- Apple - AirPods
(6, 1, 6),   -- Apple - Phụ Kiện
(7, 1, 7),   -- Apple - iPhone 16 Series
(8, 1, 8),   -- Apple - iPhone 15 Series
(9, 1, 9),   -- Apple - MacBook Pro
(10, 1, 10), -- Apple - MacBook Air
(11, 1, 11), -- Apple - iPad Pro
(12, 1, 12), -- Apple - iPad Air
(13, 2, 5),  -- Beats - AirPods/Audio
(14, 2, 6);  -- Beats - Phụ kiện

-- ==============================================================================
-- 4. BẢNG `products` (Dòng sản phẩm tổng thể)
-- Các cột: id, category_id, brand_id, name, slug, description, is_active, created_at, updated_at
-- ==============================================================================
INSERT INTO `products` (`id`, `category_id`, `brand_id`, `name`, `slug`, `description`, `is_active`, `created_at`, `updated_at`) VALUES
(1, 7, 1, 'iPhone 16 Pro', 'iphone-16-pro', 'Được tiện từ titan. Sức mạnh bứt phá cùng chip A18 Pro.', 1, '2026-10-01 09:00:00.000000', '2026-10-01 09:00:00.000000'),
(2, 7, 1, 'iPhone 16 Pro Max', 'iphone-16-pro-max', 'Màn hình 6.9 inch cực đại. Thời lượng pin tốt nhất từng có trên iPhone.', 1, '2026-10-01 09:00:00.000000', '2026-10-01 09:00:00.000000'),
(3, 7, 1, 'iPhone 16', 'iphone-16', 'Thiết kế màu sắc rực rỡ. Nút Điều Khiển Camera hoàn toàn mới.', 1, '2026-10-01 09:00:00.000000', '2026-10-01 09:00:00.000000'),
(4, 7, 1, 'iPhone 16 Plus', 'iphone-16-plus', 'Màn hình 6.7 inch siêu nét, thời lượng pin cả ngày, chip A18.', 1, '2026-10-01 09:00:00.000000', '2026-10-01 09:00:00.000000'),
(5, 8, 1, 'iPhone 15', 'iphone-15', 'Dynamic Island, camera 48MP và cổng kết nối USB-C bền bỉ.', 1, '2026-10-01 09:00:00.000000', '2026-10-01 09:00:00.000000'),
(6, 9, 1, 'MacBook Pro 14 inch M3 Pro', 'macbook-pro-14-m3-pro', 'Hiệu năng phi thường với chip M3 Pro, màn hình Liquid Retina XDR.', 1, '2026-10-01 09:00:00.000000', '2026-10-01 09:00:00.000000'),
(7, 10, 1, 'MacBook Air 13 inch M3', 'macbook-air-13-m3', 'Mỏng nhẹ kinh ngạc. Tốc độ vượt trội suốt cả ngày làm việc.', 1, '2026-10-01 09:00:00.000000', '2026-10-01 09:00:00.000000'),
(8, 11, 1, 'iPad Pro 11 inch M4', 'ipad-pro-11-m4', 'Mỏng không tưởng. Màn hình Ultra Retina XDR đột phá cùng chip M4.', 1, '2026-10-01 09:00:00.000000', '2026-10-01 09:00:00.000000'),
(9, 12, 1, 'iPad Air 11 inch M2', 'ipad-air-11-m2', 'Thiết kế thanh lịch, trang bị chip M2 mạnh mẽ đa nhiệm tuyệt đỉnh.', 1, '2026-10-01 09:00:00.000000', '2026-10-01 09:00:00.000000'),
(10, 4, 1, 'Apple Watch Ultra 2', 'apple-watch-ultra-2', 'Chiếc đồng hồ thể thao và dã ngoại tối thượng, vỏ titan siêu bền.', 1, '2026-10-01 09:00:00.000000', '2026-10-01 09:00:00.000000'),
(11, 4, 1, 'Apple Watch Series 10', 'apple-watch-series-10', 'Thiết kế mỏng nhất, màn hình lớn nhất, sạc nhanh vượt trội.', 1, '2026-10-01 09:00:00.000000', '2026-10-01 09:00:00.000000'),
(12, 5, 1, 'AirPods Pro (Thế hệ thứ 2)', 'airpods-pro-2', 'Chủ động khử tiếng ồn gấp 2 lần, cổng sạc USB-C, âm thanh Adaptive Audio.', 1, '2026-10-01 09:00:00.000000', '2026-10-01 09:00:00.000000'),
(13, 5, 1, 'AirPods Max USB-C', 'airpods-max-usbc', 'Âm thanh độ trung thực cao, âm thanh vòm cá nhân hóa, cổng sạc USB-C.', 1, '2026-10-01 09:00:00.000000', '2026-10-01 09:00:00.000000'),
(14, 6, 1, 'Củ sạc nhanh Apple 30W USB-C', 'cu-sac-nhanh-apple-30w-usb-c', 'Sạc nhanh an toàn và hiệu quả cho iPhone, iPad và MacBook Air.', 1, '2026-10-01 09:00:00.000000', '2026-10-01 09:00:00.000000'),
(15, 6, 1, 'Ốp lưng Silicone MagSafe iPhone 16 Pro', 'op-lung-silicone-magsafe-iphone-16-pro', 'Bảo vệ tối ưu, tích hợp nam châm MagSafe căn chỉnh hoàn hảo.', 1, '2026-10-01 09:00:00.000000', '2026-10-01 09:00:00.000000');

-- ==============================================================================
-- 5. BẢNG `product_variants` (Cấu hình phiên bản sản phẩm)
-- Các cột: id, product_id, sku, color, storage, ram, original_price, price, stock, image, created_at, updated_at
-- ==============================================================================
INSERT INTO `product_variants` (`id`, `product_id`, `sku`, `color`, `storage`, `ram`, `original_price`, `price`, `stock`, `image`, `created_at`, `updated_at`) VALUES
-- 1. iPhone 16 Pro (Product ID = 1)
(1, 1, 'IP16P-128-NATURAL', 'Titan Tự Nhiên', '128GB', '8GB', 28990000.00, 28990000.00, 45, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-naturaltitanium-select?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),
(2, 1, 'IP16P-256-NATURAL', 'Titan Tự Nhiên', '256GB', '8GB', 31990000.00, 31490000.00, 30, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-naturaltitanium-select?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),
(3, 1, 'IP16P-256-DESERT', 'Titan Sa Mạc', '256GB', '8GB', 31990000.00, 31990000.00, 25, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-model-unselect-gallery-2-202409?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),
(4, 1, 'IP16P-512-BLACK', 'Titan Đen', '512GB', '8GB', 37990000.00, 36990000.00, 15, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-blacktitanium-select?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),
(5, 1, 'IP16P-1TB-WHITE', 'Titan Trắng', '1TB', '8GB', 43990000.00, 42990000.00, 10, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-whitetitanium-select?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),

-- 2. iPhone 16 Pro Max (Product ID = 2)
(6, 2, 'IP16PM-256-NATURAL', 'Titan Tự Nhiên', '256GB', '8GB', 34990000.00, 34990000.00, 50, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-naturaltitanium-select?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),
(7, 2, 'IP16PM-256-DESERT', 'Titan Sa Mạc', '256GB', '8GB', 34990000.00, 34990000.00, 40, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-model-unselect-gallery-2-202409?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),
(8, 2, 'IP16PM-512-BLACK', 'Titan Đen', '512GB', '8GB', 40990000.00, 39990000.00, 20, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-blacktitanium-select?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),
(9, 2, 'IP16PM-1TB-NATURAL', 'Titan Tự Nhiên', '1TB', '8GB', 46990000.00, 45990000.00, 12, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-pro-naturaltitanium-select?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),

-- 3. iPhone 16 (Product ID = 3)
(10, 3, 'IP16-128-TEAL', 'Xanh Mòng Két', '128GB', '8GB', 22990000.00, 21990000.00, 35, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-finish-select-202409-6-1inch-teal?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),
(11, 3, 'IP16-128-PINK', 'Hồng', '128GB', '8GB', 22990000.00, 21990000.00, 30, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-finish-select-202409-6-1inch-pink?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),
(12, 3, 'IP16-256-BLACK', 'Đen', '256GB', '8GB', 25990000.00, 24990000.00, 25, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-finish-select-202409-6-1inch-black?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),

-- 4. iPhone 16 Plus (Product ID = 4)
(13, 4, 'IP16PLUS-128-BLUE', 'Xanh Lưu Ly', '128GB', '8GB', 25990000.00, 25490000.00, 20, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-finish-select-202409-6-1inch-ultramarine?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),
(14, 4, 'IP16PLUS-256-WHITE', 'Trắng', '256GB', '8GB', 28990000.00, 27990000.00, 18, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-16-finish-select-202409-6-1inch-white?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),

-- 5. iPhone 15 (Product ID = 5)
(15, 5, 'IP15-128-BLUE', 'Xanh Dương', '128GB', '6GB', 19990000.00, 18490000.00, 40, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/iphone-15-finish-select-202309-6-1inch-blue?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),

-- 6. MacBook Pro 14 inch M3 Pro (Product ID = 6)
(16, 6, 'MBP14-M3P-18-512-BLACK', 'Đen Không Gian', '512GB', '18GB', 49990000.00, 48990000.00, 15, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/mbp14-spaceblack-select-202310?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),
(17, 6, 'MBP14-M3P-18-1TB-SILVER', 'Bạc', '1TB', '18GB', 54990000.00, 53990000.00, 10, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/mbp14-silver-select-202310?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),

-- 7. MacBook Air 13 inch M3 (Product ID = 7)
(18, 7, 'MBA13-M3-8-256-MIDNIGHT', 'Xanh Đêm', '256GB', '8GB', 27990000.00, 26990000.00, 25, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/mba13-midnight-select-202402?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),
(19, 7, 'MBA13-M3-16-512-STARLIGHT', 'Ánh Sao', '512GB', '16GB', 34990000.00, 33990000.00, 20, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/mba13-starlight-select-202402?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),

-- 8. iPad Pro 11 inch M4 (Product ID = 8)
(20, 8, 'IPADP11-M4-256-BLACK', 'Đen Không Gian', '256GB', '8GB', 28990000.00, 28490000.00, 20, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/ipad-pro-finish-unselect-gallery-1-202405?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),
(21, 8, 'IPADP11-M4-512-SILVER', 'Bạc', '512GB', '8GB', 34490000.00, 33990000.00, 15, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/ipad-pro-finish-unselect-gallery-2-202405?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),

-- 9. iPad Air 11 inch M2 (Product ID = 9)
(22, 9, 'IPADA11-M2-128-PURPLE', 'Tím', '128GB', '8GB', 16990000.00, 16490000.00, 30, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/ipad-air-finish-unselect-gallery-1-202405?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),

-- 10. Apple Watch Ultra 2 (Product ID = 10)
(23, 10, 'AWU2-49-TITAN', 'Titan Tự Nhiên', '64GB', NULL, 21990000.00, 20990000.00, 18, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/ultra-case-unselect-gallery-1-202409?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),

-- 11. Apple Watch Series 10 (Product ID = 11)
(24, 11, 'AWS10-46-JETBLACK', 'Đen Bóng (Jet Black)', '64GB', NULL, 11990000.00, 11490000.00, 25, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/s10-case-unselect-gallery-1-202409?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),

-- 12. AirPods Pro 2 (Product ID = 12)
(25, 12, 'APP2-USBC-WHITE', 'Trắng', NULL, NULL, 6190000.00, 5790000.00, 60, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/airpods-pro-2-hero-select-202409?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),

-- 13. AirPods Max USB-C (Product ID = 13)
(26, 13, 'APM-USBC-MIDNIGHT', 'Midnight', NULL, NULL, 13990000.00, 13490000.00, 15, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/airpods-max-select-202409-midnight?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),

-- 14. Củ sạc nhanh Apple 30W USB-C (Product ID = 14)
(27, 14, 'ACC-CHARGER-30W', 'Trắng', NULL, NULL, 1190000.00, 1090000.00, 100, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/MU7T2_GEO_VN?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000'),

-- 15. Ốp lưng Silicone MagSafe iPhone 16 Pro (Product ID = 15)
(28, 15, 'ACC-CASE-IP16P-BLACK', 'Đen', NULL, NULL, 1429000.00, 1350000.00, 50, 'https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/MA7F4?wid=800&hei=600&fmt=jpeg&qlt=90', '2026-10-01 10:00:00.000000', '2026-10-01 10:00:00.000000');

-- ==============================================================================
-- 6. BẢNG `users` (Người dùng: admin, staff, customer)
-- Các cột: id, name, email, password, phone, address, role, created_at, updated_at
-- ==============================================================================
INSERT INTO `users` (`id`, `name`, `email`, `password`, `phone`, `address`, `role`, `created_at`, `updated_at`) VALUES
(1, 'Nguyễn Quản Trị', 'admin@dhstore.vn', '123456', '0901234567', '128 Trần Phú, Hà Đông, Hà Nội', 'admin', '2026-10-01 08:00:00.000000', '2026-10-01 08:00:00.000000'),
(2, 'Trần Vận Hành', 'staff@dhstore.vn', '123456', '0912345678', '54 Nguyễn Chí Thanh, Đống Đa, Hà Nội', 'staff', '2026-10-01 08:00:00.000000', '2026-10-01 08:00:00.000000'),
(3, 'Nguyễn Minh Quân', 'quan.nguyen@gmail.com', '123456', '0987654321', 'Số 15 Lê Duẩn, Hoàn Kiếm, Hà Nội', 'customer', '2026-10-02 08:30:00.000000', '2026-10-02 08:30:00.000000'),
(4, 'Trần Thu Hà', 'ha.tran@gmail.com', '123456', '0976543210', 'Toà Landmark 81, Bình Thạnh, TP.HCM', 'customer', '2026-10-03 09:15:00.000000', '2026-10-03 09:15:00.000000'),
(5, 'Lê Hoàng Nam', 'nam.le@gmail.com', '123456', '0965432109', '280 An Dương Vương, Quận 5, TP.HCM', 'customer', '2026-10-04 10:00:00.000000', '2026-10-04 10:00:00.000000');

-- ==============================================================================
-- 7. BẢNG `carts` (Túi hàng / Giỏ hàng của từng khách hàng)
-- Các cột: id, user_id, created_at, updated_at
-- ==============================================================================
INSERT INTO `carts` (`id`, `user_id`, `created_at`, `updated_at`) VALUES
(1, 3, '2026-10-02 08:35:00.000000', '2026-10-02 08:35:00.000000'),
(2, 4, '2026-10-03 09:20:00.000000', '2026-10-03 09:20:00.000000'),
(3, 5, '2026-10-04 10:05:00.000000', '2026-10-04 10:05:00.000000');

-- ==============================================================================
-- 8. BẢNG `cart_items` (Chi tiết dòng sản phẩm trong túi)
-- Các cột: id, cart_id, product_variant_id, quantity, created_at, updated_at
-- ==============================================================================
INSERT INTO `cart_items` (`id`, `cart_id`, `product_variant_id`, `quantity`, `created_at`, `updated_at`) VALUES
(1, 1, 3, 1, '2026-10-02 09:00:00.000000', '2026-10-02 09:00:00.000000'),  -- Quân có iPhone 16 Pro 256GB Desert
(2, 1, 25, 1, '2026-10-02 09:05:00.000000', '2026-10-02 09:05:00.000000'), -- Quân có AirPods Pro 2
(3, 2, 16, 1, '2026-10-03 10:00:00.000000', '2026-10-03 10:00:00.000000'), -- Hà có MacBook Pro 14 inch
(4, 2, 27, 1, '2026-10-03 10:05:00.000000', '2026-10-03 10:05:00.000000'), -- Hà có Củ sạc nhanh 30W
(5, 3, 20, 1, '2026-10-04 11:00:00.000000', '2026-10-04 11:00:00.000000'); -- Nam có iPad Pro 11 inch

-- ==============================================================================
-- 9. BẢNG `orders` (Đơn hàng)
-- Các cột: id, user_id, order_code, total_amount, payment_method, payment_status,
--         order_status, shipping_address, shipping_phone, note, created_at, updated_at
-- ==============================================================================
INSERT INTO `orders` (`id`, `user_id`, `order_code`, `total_amount`, `payment_method`, `payment_status`, `order_status`, `shipping_address`, `shipping_phone`, `note`, `created_at`, `updated_at`) VALUES
(1, 3, 'ORD-20261018-9042', 37780000.00, 'vnpay', 'PAID', 'Delivered', 'Số 15 Lê Duẩn, Hoàn Kiếm, Hà Nội', '0987654321', 'Giao giờ hành chính, gọi trước khi đến.', '2026-10-18 09:15:00.000000', '2026-10-19 14:30:00.000000'),
(2, 4, 'ORD-20261018-9041', 50080000.00, 'momo', 'PAID', 'Shipped', 'Toà Landmark 81, Bình Thạnh, TP.HCM', '0976543210', 'Gửi lễ tân toà nhà giúp tôi.', '2026-10-18 08:40:00.000000', '2026-10-19 10:00:00.000000'),
(3, 5, 'ORD-20261017-9040', 28490000.00, 'cod', 'PENDING', 'Processing', '280 An Dương Vương, Quận 5, TP.HCM', '0965432109', 'Kiểm tra máy trước khi thanh toán COD.', '2026-10-17 19:22:00.000000', '2026-10-18 08:00:00.000000'),
(4, 3, 'ORD-20261025-9043', 34990000.00, 'vnpay', 'PAID', 'Pending', 'Số 15 Lê Duẩn, Hoàn Kiếm, Hà Nội', '0987654321', 'Yêu cầu xuất hoá đơn điện tử VAT.', '2026-10-25 15:30:00.000000', '2026-10-25 15:30:00.000000'),
(5, 4, 'ORD-20261026-9044', 13490000.00, 'cod', 'FAILED', 'Cancelled', 'Toà Landmark 81, Bình Thạnh, TP.HCM', '0976543210', 'Khách hàng đổi ý mua phiên bản khác.', '2026-10-26 11:10:00.000000', '2026-10-26 12:00:00.000000');

-- ==============================================================================
-- 10. BẢNG `order_items` (Chi tiết các mặt hàng trong đơn hàng)
-- Các cột: id, order_id, product_variant_id, quantity, price, total_price
-- ==============================================================================
INSERT INTO `order_items` (`id`, `order_id`, `product_variant_id`, `quantity`, `price`, `total_price`) VALUES
-- Đơn hàng 1 (Tổng: 37,780,000đ)
(1, 1, 3, 1, 31990000.00, 31990000.00),   -- 1 x iPhone 16 Pro 256GB Titan Sa Mạc
(2, 1, 25, 1, 5790000.00, 5790000.00),     -- 1 x AirPods Pro 2

-- Đơn hàng 2 (Tổng: 50,080,000đ)
(3, 2, 16, 1, 48990000.00, 48990000.00),  -- 1 x MacBook Pro 14 M3 Pro
(4, 2, 27, 1, 1090000.00, 1090000.00),    -- 1 x Củ sạc nhanh 30W

-- Đơn hàng 3 (Tổng: 28,490,000đ)
(5, 3, 20, 1, 28490000.00, 28490000.00),  -- 1 x iPad Pro 11 inch M4

-- Đơn hàng 4 (Tổng: 34,990,000đ)
(6, 4, 6, 1, 34990000.00, 34990000.00),   -- 1 x iPhone 16 Pro Max 256GB Titan Tự Nhiên

-- Đơn hàng 5 (Tổng: 13,490,000đ)
(7, 5, 26, 1, 13490000.00, 13490000.00);  -- 1 x AirPods Max Midnight

SET FOREIGN_KEY_CHECKS = 1;

-- Hoàn tất seed dữ liệu mẫu!
