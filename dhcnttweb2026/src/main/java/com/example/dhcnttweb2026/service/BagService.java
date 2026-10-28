package com.example.dhcnttweb2026.service;

import com.example.dhcnttweb2026.controller.ClientBagController.AccessoryUpsellDto;
import com.example.dhcnttweb2026.controller.ClientBagController.BagItemDto;
import com.example.dhcnttweb2026.entity.Cart;
import com.example.dhcnttweb2026.entity.CartItem;
import com.example.dhcnttweb2026.entity.Product;
import com.example.dhcnttweb2026.entity.ProductVariant;
import com.example.dhcnttweb2026.entity.User;
import com.example.dhcnttweb2026.repository.CartItemRepository;
import com.example.dhcnttweb2026.repository.CartRepository;
import com.example.dhcnttweb2026.repository.ProductRepository;
import com.example.dhcnttweb2026.repository.ProductVariantRepository;
import com.example.dhcnttweb2026.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@SuppressWarnings("null")
public class BagService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public BagService(CartRepository cartRepository,
                      CartItemRepository cartItemRepository,
                      ProductVariantRepository productVariantRepository,
                      ProductRepository productRepository,
                      UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productVariantRepository = productVariantRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public record BagViewDto(
            List<BagItemDto> items,
            long subtotal,
            String formattedSubtotal,
            long shippingFee,
            String formattedShippingFee,
            long total,
            String formattedTotal,
            int totalItemsCount,
            List<AccessoryUpsellDto> compatibleAccessories,
            boolean isBagEmpty
    ) {}

    public record AddToBagResult(
            boolean success,
            String message,
            int totalItems,
            long lineTotal,
            String formattedLineTotal
    ) {}

    public record UpdateBagItemResult(
            boolean success,
            String message,
            long lineTotal,
            String formattedLineTotal,
            long subtotal,
            String formattedSubtotal,
            long total,
            String formattedTotal,
            int totalItems,
            boolean itemRemoved
    ) {}

    @Transactional
    public Cart getOrCreateCart(String guestToken, Long userId) {
        if (userId != null) {
            Optional<Cart> userCartOpt = cartRepository.findByUserId(userId);
            Cart userCart = userCartOpt.orElseGet(() -> {
                User user = userRepository.findById(userId).orElse(null);
                Cart newCart = new Cart(user);
                return cartRepository.save(newCart);
            });

            // ADR 0002: Hợp nhất (Merge Bag) nếu có guest_token từ phiên vãng lai trước đó
            if (guestToken != null && !guestToken.isBlank()) {
                Optional<Cart> guestCartOpt = cartRepository.findByGuestToken(guestToken);
                if (guestCartOpt.isPresent() && !Objects.equals(guestCartOpt.get().getId(), userCart.getId())) {
                    Cart guestCart = guestCartOpt.get();
                    List<CartItem> guestItems = cartItemRepository.findByCartId(guestCart.getId());
                    for (CartItem gItem : guestItems) {
                        if (gItem == null || gItem.getProductVariant() == null) continue;
                        Optional<CartItem> existingInUser = cartItemRepository
                                .findByCartIdAndProductVariantId(userCart.getId(), gItem.getProductVariant().getId());
                        if (existingInUser.isPresent()) {
                            CartItem uItem = existingInUser.get();
                            int uQty = uItem.getQuantity() != null ? uItem.getQuantity() : 0;
                            int gQty = gItem.getQuantity() != null ? gItem.getQuantity() : 0;
                            uItem.setQuantity(uQty + gQty);
                            cartItemRepository.save(uItem);
                        } else {
                            gItem.setCart(userCart);
                            cartItemRepository.save(gItem);
                        }
                    }
                    cartRepository.delete(guestCart);
                }
            }
            return userCart;
        }

        if (guestToken != null && !guestToken.isBlank()) {
            return cartRepository.findByGuestToken(guestToken)
                    .orElseGet(() -> cartRepository.save(new Cart(guestToken)));
        }

        // Tạo token mới cho khách vãng lai
        String newToken = UUID.randomUUID().toString();
        return cartRepository.save(new Cart(newToken));
    }

    @Transactional(readOnly = true)
    public BagViewDto getBagView(String guestToken, Long userId) {
        Cart cart = findCart(guestToken, userId);
        if (cart == null) {
            return new BagViewDto(
                    Collections.emptyList(), 0L, "0đ", 0L, "Miễn phí", 0L, "0đ", 0,
                    getCompatibleAccessoriesUpsell(), true
            );
        }

        List<CartItem> cartItems = cartItemRepository.findByCartIdWithProductDetails(cart.getId());
        List<BagItemDto> itemDtos = new ArrayList<>();
        long subtotal = 0;
        int totalCount = 0;

        for (CartItem ci : cartItems) {
            if (ci == null) continue;
            ProductVariant pv = ci.getProductVariant();
            if (pv == null) continue;
            Product p = pv.getProduct();
            if (p == null) continue;

            long unitPrice = (pv.getPrice() != null) ? pv.getPrice().longValue() : 0L;
            int qty = (ci.getQuantity() != null) ? ci.getQuantity() : 1;
            long lineTotal = unitPrice * qty;

            subtotal += lineTotal;
            totalCount += qty;

            String colorName = (pv.getColor() != null && !pv.getColor().isBlank()) ? pv.getColor() : "Tiêu chuẩn";
            String storage = (pv.getStorage() != null && !pv.getStorage().isBlank()) ? pv.getStorage() : "Tiêu chuẩn";

            String productName = p.getName() != null ? p.getName() : "Sản phẩm Apple";
            if (!"Tiêu chuẩn".equals(storage)) {
                productName += " " + storage;
            }

            itemDtos.add(new BagItemDto(
                    ci.getId(),
                    pv.getSku(),
                    productName,
                    colorName,
                    ClientStorefrontService.resolveColorHex(colorName),
                    storage,
                    pv.getImage() != null ? pv.getImage() : "",
                    unitPrice,
                    ClientStorefrontService.formatVND(unitPrice),
                    qty,
                    pv.getStock() != null ? pv.getStock() : 0,
                    lineTotal,
                    ClientStorefrontService.formatVND(lineTotal),
                    "/product/" + (p.getSlug() != null ? p.getSlug() : "")
            ));
        }

        long shippingFee = 0L;
        long total = subtotal + shippingFee;

        return new BagViewDto(
                itemDtos,
                subtotal,
                ClientStorefrontService.formatVND(subtotal),
                shippingFee,
                "Miễn phí",
                total,
                ClientStorefrontService.formatVND(total),
                totalCount,
                getCompatibleAccessoriesUpsell(),
                itemDtos.isEmpty()
        );
    }

    @Transactional
    public AddToBagResult addToBag(String guestToken, Long userId, Long variantId, String sku, int quantity) {
        if (quantity <= 0) quantity = 1;

        ProductVariant variant = null;
        if (variantId != null) {
            variant = productVariantRepository.findByIdWithProduct(variantId).orElse(null);
        }
        if (variant == null && sku != null && !sku.isBlank()) {
            variant = productVariantRepository.findBySkuWithProduct(sku).orElse(null);
        }

        if (variant == null) {
            return new AddToBagResult(false, "Không tìm thấy phiên bản sản phẩm.", 0, 0L, "0đ");
        }

        Cart cart = getOrCreateCart(guestToken, userId);

        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartIdAndProductVariantId(cart.getId(), variant.getId());
        CartItem cartItem;
        if (existingItemOpt.isPresent()) {
            cartItem = existingItemOpt.get();
            int currentQty = cartItem.getQuantity() != null ? cartItem.getQuantity() : 0;
            int maxStock = (variant.getStock() != null) ? variant.getStock() : 99;
            cartItem.setQuantity(Math.min(maxStock, currentQty + quantity));
            cartItemRepository.save(cartItem);
        } else {
            int maxStock = (variant.getStock() != null) ? variant.getStock() : 99;
            cartItem = new CartItem(null, cart, variant, Math.min(maxStock, quantity));
            cartItemRepository.save(cartItem);
        }

        long unitPrice = (variant.getPrice() != null) ? variant.getPrice().longValue() : 0L;
        int itemQty = cartItem.getQuantity() != null ? cartItem.getQuantity() : 1;
        long lineTotal = unitPrice * itemQty;

        int totalCount = getTotalItemCount(guestToken, userId);

        return new AddToBagResult(
                true,
                "Đã thêm sản phẩm vào túi hàng thành công.",
                totalCount,
                lineTotal,
                ClientStorefrontService.formatVND(lineTotal)
        );
    }

    @Transactional
    public UpdateBagItemResult updateQuantity(Long cartItemId, int newQuantity) {
        if (cartItemId == null) {
            return new UpdateBagItemResult(false, "Mã sản phẩm không hợp lệ.", 0, "0đ", 0, "0đ", 0, "0đ", 0, false);
        }
        Optional<CartItem> opt = cartItemRepository.findById(cartItemId);
        if (opt.isEmpty()) {
            return new UpdateBagItemResult(false, "Không tìm thấy dòng sản phẩm trong túi.", 0, "0đ", 0, "0đ", 0, "0đ", 0, false);
        }

        CartItem item = opt.get();
        Cart cart = item.getCart();

        if (newQuantity <= 0) {
            cartItemRepository.delete(item);
            BagViewDto view = (cart != null) ? calculateTotals(cart.getId()) : null;
            long sub = (view != null) ? view.subtotal() : 0L;
            String fSub = (view != null) ? view.formattedSubtotal() : "0đ";
            long tot = (view != null) ? view.total() : 0L;
            String fTot = (view != null) ? view.formattedTotal() : "0đ";
            int cnt = (view != null) ? view.totalItemsCount() : 0;
            return new UpdateBagItemResult(
                    true, "Đã xóa sản phẩm khỏi túi hàng.", 0L, "0đ",
                    sub, fSub, tot, fTot, cnt, true
            );
        }

        ProductVariant pv = item.getProductVariant();
        int maxStock = (pv != null && pv.getStock() != null) ? pv.getStock() : 99;
        item.setQuantity(Math.min(maxStock, newQuantity));
        cartItemRepository.save(item);

        long unitPrice = (pv != null && pv.getPrice() != null) ? pv.getPrice().longValue() : 0L;
        int itemQty = item.getQuantity() != null ? item.getQuantity() : 1;
        long lineTotal = unitPrice * itemQty;

        BagViewDto view = (cart != null) ? calculateTotals(cart.getId()) : null;
        long sub = (view != null) ? view.subtotal() : lineTotal;
        String fSub = (view != null) ? view.formattedSubtotal() : ClientStorefrontService.formatVND(lineTotal);
        long tot = (view != null) ? view.total() : lineTotal;
        String fTot = (view != null) ? view.formattedTotal() : ClientStorefrontService.formatVND(lineTotal);
        int cnt = (view != null) ? view.totalItemsCount() : itemQty;

        return new UpdateBagItemResult(
                true, "Cập nhật số lượng thành công.", lineTotal, ClientStorefrontService.formatVND(lineTotal),
                sub, fSub, tot, fTot, cnt, false
        );
    }

    @Transactional
    public UpdateBagItemResult removeItem(Long cartItemId) {
        return updateQuantity(cartItemId, 0);
    }

    @Transactional(readOnly = true)
    public int getTotalItemCount(String guestToken, Long userId) {
        Cart cart = findCart(guestToken, userId);
        if (cart == null) return 0;
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        return items.stream()
                .mapToInt(ci -> (ci != null && ci.getQuantity() != null) ? ci.getQuantity() : 0)
                .sum();
    }

    private Cart findCart(String guestToken, Long userId) {
        if (userId != null) {
            return cartRepository.findByUserId(userId).orElse(null);
        }
        if (guestToken != null && !guestToken.isBlank()) {
            return cartRepository.findByGuestToken(guestToken).orElse(null);
        }
        return null;
    }

    private BagViewDto calculateTotals(Long cartId) {
        if (cartId == null) {
            return new BagViewDto(
                    Collections.emptyList(), 0L, "0đ", 0L, "Miễn phí", 0L, "0đ", 0,
                    Collections.emptyList(), true
            );
        }
        List<CartItem> items = cartItemRepository.findByCartIdWithProductDetails(cartId);
        long subtotal = 0;
        int count = 0;
        for (CartItem ci : items) {
            if (ci == null) continue;
            ProductVariant pv = ci.getProductVariant();
            long p = (pv != null && pv.getPrice() != null) ? pv.getPrice().longValue() : 0L;
            int qty = (ci.getQuantity() != null) ? ci.getQuantity() : 0;
            subtotal += p * qty;
            count += qty;
        }
        long shipping = 0L;
        long total = subtotal + shipping;
        return new BagViewDto(
                Collections.emptyList(), subtotal, ClientStorefrontService.formatVND(subtotal),
                shipping, "Miễn phí", total, ClientStorefrontService.formatVND(total), count,
                Collections.emptyList(), items.isEmpty()
        );
    }

    @Transactional(readOnly = true)
    public List<AccessoryUpsellDto> getCompatibleAccessoriesUpsell() {
        List<Product> accessories = productRepository.findByCategoryIdAndIsActiveTrue(6L);
        if (accessories.isEmpty()) {
            accessories = productRepository.findAllActiveWithDetails();
        }

        List<AccessoryUpsellDto> result = new ArrayList<>();
        for (Product p : accessories) {
            if (p == null) continue;
            if (result.size() >= 4) break;

            ProductVariant firstVar = (p.getVariants() != null && !p.getVariants().isEmpty()) ? p.getVariants().get(0) : null;
            long price = 0L;
            String sku = "ACC-" + p.getId();
            String img = "https://store.storeimages.cdn-apple.com/8756/as-images.apple.com/is/MU7T2_GEO_VN?wid=400&hei=400&fmt=jpeg&qlt=90";

            if (firstVar != null) {
                if (firstVar.getPrice() != null) price = firstVar.getPrice().longValue();
                if (firstVar.getSku() != null) sku = firstVar.getSku();
                if (firstVar.getImage() != null && !firstVar.getImage().isBlank()) img = firstVar.getImage();
            }

            result.add(new AccessoryUpsellDto(
                    p.getId(),
                    sku,
                    p.getName() != null ? p.getName() : "Phụ kiện",
                    (p.getCategory() != null) ? p.getCategory().getName() : "Phụ kiện",
                    img,
                    price,
                    ClientStorefrontService.formatVND(price),
                    "/product/" + (p.getSlug() != null ? p.getSlug() : "")
            ));
        }

        return result;
    }
}
