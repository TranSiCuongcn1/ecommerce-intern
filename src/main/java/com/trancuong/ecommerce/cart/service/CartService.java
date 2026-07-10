package com.trancuong.ecommerce.cart.service;

import com.trancuong.ecommerce.cart.domain.CartItem;
import com.trancuong.ecommerce.cart.dto.CartItemQuantityRequest;
import com.trancuong.ecommerce.cart.dto.CartItemRequest;
import com.trancuong.ecommerce.cart.dto.CartItemResponse;
import com.trancuong.ecommerce.cart.dto.CartItemResponse.ProductSummary;
import com.trancuong.ecommerce.cart.dto.CartResponse;
import com.trancuong.ecommerce.cart.exception.CartItemNotFoundException;
import com.trancuong.ecommerce.cart.exception.ProductNotAvailableForCartException;
import com.trancuong.ecommerce.cart.repository.CartItemRepository;
import com.trancuong.ecommerce.product.domain.Product;
import com.trancuong.ecommerce.product.exception.ProductNotFoundException;
import com.trancuong.ecommerce.product.repository.ProductRepository;
import com.trancuong.ecommerce.user.domain.User;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartResponse getCart(User user) {
        return toCartResponse(cartItemRepository.findByUserIdOrderByCreatedAtDesc(user.getId()));
    }

    @Transactional
    public CartResponse addItem(User user, CartItemRequest request) {
        Product product = getActiveProduct(request.productId());

        cartItemRepository.findByUserIdAndProductId(user.getId(), product.getId())
                .ifPresentOrElse(
                        item -> item.increaseQuantity(request.quantity()),
                        () -> cartItemRepository.save(new CartItem(user, product, request.quantity()))
                );
        cartItemRepository.flush();

        return getCart(user);
    }

    @Transactional
    public CartResponse updateItemQuantity(User user, UUID id, CartItemQuantityRequest request) {
        CartItem item = getCartItem(user, id);
        if (!"ACTIVE".equalsIgnoreCase(item.getProduct().getStatus())) {
            throw new ProductNotAvailableForCartException(item.getProduct().getId());
        }

        item.updateQuantity(request.quantity());
        cartItemRepository.flush();
        return getCart(user);
    }

    @Transactional
    public void removeItem(User user, UUID id) {
        CartItem item = getCartItem(user, id);
        cartItemRepository.delete(item);
        cartItemRepository.flush();
    }

    @Transactional
    public void clearCart(User user) {
        cartItemRepository.deleteByUserId(user.getId());
        cartItemRepository.flush();
    }

    private CartItem getCartItem(User user, UUID id) {
        return cartItemRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new CartItemNotFoundException(id));
    }

    private Product getActiveProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        if (!"ACTIVE".equalsIgnoreCase(product.getStatus())) {
            throw new ProductNotAvailableForCartException(id);
        }
        return product;
    }

    private CartResponse toCartResponse(List<CartItem> items) {
        List<CartItemResponse> itemResponses = items.stream()
                .map(this::toItemResponse)
                .toList();
        Integer totalItems = itemResponses.stream()
                .map(CartItemResponse::quantity)
                .reduce(0, Integer::sum);
        BigDecimal totalAmount = itemResponses.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(itemResponses, totalItems, totalAmount);
    }

    private CartItemResponse toItemResponse(CartItem item) {
        Product product = item.getProduct();
        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new CartItemResponse(
                item.getId(),
                new ProductSummary(
                        product.getId(),
                        product.getName(),
                        product.getSlug(),
                        product.getImageUrl(),
                        product.getStatus()
                ),
                item.getQuantity(),
                product.getPrice(),
                subtotal,
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
