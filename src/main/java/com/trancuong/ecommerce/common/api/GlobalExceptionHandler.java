package com.trancuong.ecommerce.common.api;

import com.trancuong.ecommerce.auth.exception.DuplicateEmailException;
import com.trancuong.ecommerce.auth.exception.InvalidCredentialsException;
import com.trancuong.ecommerce.auth.exception.InvalidRefreshTokenException;
import com.trancuong.ecommerce.cart.exception.CartItemNotFoundException;
import com.trancuong.ecommerce.cart.exception.ProductNotAvailableForCartException;
import com.trancuong.ecommerce.category.exception.CategoryNotFoundException;
import com.trancuong.ecommerce.category.exception.DuplicateCategorySlugException;
import com.trancuong.ecommerce.inventory.exception.DuplicateInventoryException;
import com.trancuong.ecommerce.inventory.exception.InsufficientInventoryException;
import com.trancuong.ecommerce.inventory.exception.InventoryNotFoundException;
import com.trancuong.ecommerce.media.exception.MediaUploadException;
import com.trancuong.ecommerce.order.exception.CheckoutAddressNotFoundException;
import com.trancuong.ecommerce.order.exception.EmptyCartException;
import com.trancuong.ecommerce.order.exception.InvalidOrderStatusException;
import com.trancuong.ecommerce.order.exception.OrderNotFoundException;
import com.trancuong.ecommerce.product.exception.DuplicateProductSlugException;
import com.trancuong.ecommerce.product.exception.ProductNotFoundException;
import com.trancuong.ecommerce.user.exception.UserAddressNotFoundException;
import com.trancuong.ecommerce.warehouse.exception.DuplicateWarehouseCodeException;
import com.trancuong.ecommerce.warehouse.exception.WarehouseNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            CategoryNotFoundException exception
    ) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(WarehouseNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            WarehouseNotFoundException exception
    ) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            ProductNotFoundException exception
    ) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            InventoryNotFoundException exception
    ) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(UserAddressNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            UserAddressNotFoundException exception
    ) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            CartItemNotFoundException exception
    ) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(DuplicateCategorySlugException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateSlug(
            DuplicateCategorySlugException exception
    ) {
        return response(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(DuplicateWarehouseCodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateCode(
            DuplicateWarehouseCodeException exception
    ) {
        return response(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(DuplicateProductSlugException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateSlug(
            DuplicateProductSlugException exception
    ) {
        return response(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(DuplicateInventoryException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateInventory(
            DuplicateInventoryException exception
    ) {
        return response(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(InsufficientInventoryException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientInventory(
            InsufficientInventoryException exception
    ) {
        return response(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(ProductNotAvailableForCartException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductNotAvailableForCart(
            ProductNotAvailableForCartException exception
    ) {
        return response(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(EmptyCartException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmptyCart(
            EmptyCartException exception
    ) {
        return response(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(CheckoutAddressNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCheckoutAddressNotFound(
            CheckoutAddressNotFoundException exception
    ) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            OrderNotFoundException exception
    ) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(InvalidOrderStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidOrderStatus(
            InvalidOrderStatusException exception
    ) {
        return response(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(MediaUploadException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaUpload(
            MediaUploadException exception
    ) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateEmail(
            DuplicateEmailException exception
    ) {
        return response(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler({
            InvalidCredentialsException.class,
            InvalidRefreshTokenException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(
            RuntimeException exception
    ) {
        return response(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(
            DataIntegrityViolationException exception
    ) {
        return response(
                HttpStatus.CONFLICT,
                "The operation conflicts with existing data"
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Request validation failed");
        return response(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(
            Exception exception
    ) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    private ResponseEntity<ApiResponse<Void>> response(
            HttpStatus status,
            String message
    ) {
        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(status.value(), message));
    }
}
