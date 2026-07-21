# Swagger Test Script

Use this script in Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

## Quick Full Flow

Use this compact flow when you want to verify the happy path from account setup to checkout.
Catalog, warehouse, and inventory mutation APIs require an `ADMIN` token. Use a customer token for `/api/me`, cart, checkout, and customer order APIs.

```http
GET /api/health
```

Expected: `200 OK`.

```http
POST /api/auth/register
```

```json
{
  "fullName": "Test Customer",
  "email": "customer@example.com",
  "password": "password123"
}
```

Expected: `201 Created`. Copy `accessToken`, then click `Authorize` and paste only the raw token:

```text
<accessToken>
```

```http
POST /api/me/addresses
```

```json
{
  "receiverName": "Test Customer",
  "receiverPhone": "0900000000",
  "province": "Ho Chi Minh",
  "district": "District 1",
  "ward": "Ben Nghe",
  "detailAddress": "123 Nguyen Hue",
  "defaultAddress": true
}
```

Expected: `201 Created`. Copy `id` as `addressId`.

```http
POST /api/categories
```

```json
{
  "name": "Phones",
  "slug": "phones"
}
```

Expected: `201 Created`. Copy `id` as `categoryId`.

```http
POST /api/products
```

```json
{
  "categoryId": "<categoryId>",
  "name": "iPhone 15",
  "slug": "iphone-15",
  "description": "Apple smartphone",
  "price": 19990000,
  "imageUrl": "https://example.com/iphone-15.jpg",
  "status": "ACTIVE"
}
```

Expected: `201 Created`. Copy `id` as `productId`.

```http
POST /api/warehouses
```

```json
{
  "code": "HCM-01",
  "name": "Ho Chi Minh Warehouse",
  "address": "District 1, Ho Chi Minh",
  "status": "ACTIVE"
}
```

Expected: `201 Created`. Copy `id` as `warehouseId`.

```http
POST /api/inventory
```

```json
{
  "productId": "<productId>",
  "warehouseId": "<warehouseId>",
  "quantityOnHand": 100,
  "quantityReserved": 0,
  "reorderLevel": 10
}
```

Expected: `201 Created`. Copy `id` as `inventoryId`.

```http
POST /api/cart/items
```

```json
{
  "productId": "<productId>",
  "quantity": 2
}
```

Expected: `201 Created`. `totalItems` should be `2`.

```http
POST /api/orders/checkout
```

```json
{
  "addressId": "<addressId>",
  "paymentMethod": "COD",
  "shippingFee": 0
}
```

Expected: `200 OK`. Copy returned `id` as `orderId`.

```http
GET /api/cart
```

Expected: `200 OK` with empty `items`.

```http
GET /api/inventory/{inventoryId}
```

Expected: `quantityOnHand` decreased from `100` to `98`.

```http
GET /api/orders
GET /api/orders/{orderId}
```

Expected: `200 OK`; the order detail includes the checked-out item.

## 0. Health Check

```http
GET /api/health
```

Expected: `200 OK`.

## 1. Auth Module

### Register User

```http
POST /api/auth/register
```

Body:

```json
{
  "fullName": "Test Customer",
  "email": "customer@example.com",
  "password": "password123"
}
```

Expected: `201 Created`. Copy `accessToken` and `refreshToken`.

### Login

```http
POST /api/auth/login
```

Body:

```json
{
  "email": "customer@example.com",
  "password": "password123"
}
```

Expected: `200 OK`.

### Authorize Swagger

Click `Authorize` and input:

```text
<accessToken>
```

### Refresh Token

```http
POST /api/auth/refresh
```

Body:

```json
{
  "refreshToken": "<refreshToken>"
}
```

Expected: `200 OK`.

### Logout

```http
POST /api/auth/logout
```

Body:

```json
{
  "refreshToken": "<refreshToken>"
}
```

Expected: `204 No Content`.

## 2. User Profile Module

### Get Current Profile

```http
GET /api/me
```

Expected: `200 OK`.

### Get Addresses

```http
GET /api/me/addresses
```

Expected: `200 OK`.

### Create Address

```http
POST /api/me/addresses
```

Body:

```json
{
  "receiverName": "Test Customer",
  "receiverPhone": "0900000000",
  "province": "Ho Chi Minh",
  "district": "District 1",
  "ward": "Ben Nghe",
  "detailAddress": "123 Nguyen Hue",
  "defaultAddress": true
}
```

Expected: `201 Created`. Copy returned `id` as `addressId`.

### Update Address

```http
PUT /api/me/addresses/{id}
```

Expected: `200 OK`.

### Set Default Address

```http
PATCH /api/me/addresses/{id}/default
```

Expected: `200 OK`.

### Delete Address

```http
DELETE /api/me/addresses/{id}
```

Expected: `204 No Content`.

## 3. Category Module

### Create Category

```http
POST /api/categories
```

Body:

```json
{
  "name": "Phones",
  "slug": "phones"
}
```

Expected: `201 Created`. Copy returned `id` as `categoryId`.

### Get Categories

```http
GET /api/categories
GET /api/categories?keyword=phone
```

Expected: `200 OK`.

### Get Category Detail

```http
GET /api/categories/{id}
```

Expected: `200 OK`.

### Update Category

```http
PUT /api/categories/{id}
```

Expected: `200 OK`.

### Delete Category

```http
DELETE /api/categories/{id}
```

Expected: `204 No Content`.

## 4. Product Module

### Create Product

```http
POST /api/products
```

Body:

```json
{
  "categoryId": "<categoryId>",
  "name": "iPhone 15",
  "slug": "iphone-15",
  "description": "Apple smartphone",
  "price": 19990000,
  "imageUrl": "https://example.com/iphone-15.jpg",
  "status": "ACTIVE"
}
```

Expected: `201 Created`. Copy returned `id` as `productId`.

### Get Products

```http
GET /api/products
GET /api/products?keyword=iphone
GET /api/products?categoryId=<categoryId>
GET /api/products?status=ACTIVE
```

Expected: `200 OK`.

### Get Product Detail

```http
GET /api/products/{id}
```

Expected: `200 OK`.

### Update Product

```http
PUT /api/products/{id}
```

Expected: `200 OK`.

### Delete Product

```http
DELETE /api/products/{id}
```

Expected: `204 No Content`.

## 5. Warehouse Module

### Create Warehouse

```http
POST /api/warehouses
```

Body:

```json
{
  "code": "HCM-01",
  "name": "Ho Chi Minh Warehouse",
  "address": "District 1, Ho Chi Minh",
  "status": "ACTIVE"
}
```

Expected: `201 Created`. Copy returned `id` as `warehouseId`.

### Get Warehouses

```http
GET /api/warehouses
GET /api/warehouses?keyword=hcm
GET /api/warehouses?status=ACTIVE
```

Expected: `200 OK`.

### Get Warehouse Detail

```http
GET /api/warehouses/{id}
```

Expected: `200 OK`.

### Update Warehouse

```http
PUT /api/warehouses/{id}
```

Expected: `200 OK`.

### Delete Warehouse

```http
DELETE /api/warehouses/{id}
```

Expected: `204 No Content`.

## 6. Inventory Module

### Create Inventory

```http
POST /api/inventory
```

Body:

```json
{
  "productId": "<productId>",
  "warehouseId": "<warehouseId>",
  "quantityOnHand": 100,
  "quantityReserved": 0,
  "reorderLevel": 10
}
```

Expected: `201 Created`. Copy returned `id` as `inventoryId`.

### Get Inventory

```http
GET /api/inventory
GET /api/inventory?productId=<productId>
GET /api/inventory?warehouseId=<warehouseId>
GET /api/inventory?availableOnly=true
GET /api/inventory?lowStockOnly=true
```

Expected: `200 OK`.

### Get Inventory Detail

```http
GET /api/inventory/{id}
```

Expected: `200 OK`.

### Allocate Inventory

```http
POST /api/inventory/allocate
```

Body:

```json
{
  "productId": "<productId>",
  "quantity": 2
}
```

Expected: `200 OK`. `quantityReserved` increases and `availableQuantity` decreases.

### Update Inventory

```http
PUT /api/inventory/{id}
```

Expected: `200 OK`.

### Delete Inventory

```http
DELETE /api/inventory/{id}
```

Expected: `204 No Content`.

## 7. Cart Module

### Get Cart

```http
GET /api/cart
```

Expected: `200 OK`.

### Add Cart Item

```http
POST /api/cart/items
```

Body:

```json
{
  "productId": "<productId>",
  "quantity": 2
}
```

Expected: `201 Created`. Copy returned item `id` as `cartItemId`.

If you want to test checkout, keep at least one item in the cart before continuing.

### Update Cart Item Quantity

```http
PUT /api/cart/items/{id}
```

Body:

```json
{
  "quantity": 3
}
```

Expected: `200 OK`.

### Remove Cart Item

```http
DELETE /api/cart/items/{id}
```

Expected: `200 OK`.

### Clear Cart

```http
DELETE /api/cart
```

Expected: `200 OK`.

## 8. Order Module

### Get My Orders

```http
GET /api/orders
GET /api/orders?page=0&size=10
```

Expected: `200 OK`.

### Get My Order Detail

```http
GET /api/orders/{id}
```

Expected: `200 OK`.

### Checkout

```http
POST /api/orders/checkout
```

Body:

```json
{
  "addressId": "<addressId>",
  "paymentMethod": "COD",
  "shippingFee": 0
}
```

Expected: `200 OK`. The API creates an order from the current cart, creates order items, creates a pending payment, deducts inventory, and clears the cart.

Allowed payment methods:

```text
COD
BANK_TRANSFER
MOMO
VNPAY
```

You can also omit `addressId` to use the default address.

## 9. Admin Order Module

These APIs require an `ADMIN` token.

### Get All Orders

```http
GET /api/admin/orders
GET /api/admin/orders?page=0&size=10
```

Expected: `200 OK`.

### Get Order Detail

```http
GET /api/admin/orders/{id}
```

Expected: `200 OK`.

### Update Order Status

```http
PATCH /api/admin/orders/{id}/status
```

Body:

```json
{
  "status": "CONFIRMED"
}
```

Expected: `200 OK`.

Allowed statuses:

```text
PENDING
CONFIRMED
SHIPPING
COMPLETED
CANCELLED
```

If status is changed to `CANCELLED`, the API restores inventory for the order items.

## 10. Admin Media Module

These APIs require an `ADMIN` token.

### Upload Media

```http
POST /api/admin/media/upload
```

Use `multipart/form-data` with a `file` part.

Expected: `200 OK`. The response includes `objectName`, `url`, `contentType`, and `size`.

## 11. MoMo Payment Module (Sandbox - Backend Only)

This flow allows you to test the MoMo Payment Gateway completely from Swagger UI and your browser without a frontend.

### Step 1: Customer Login
Use the `auth-controller` in Swagger:
```http
POST /api/auth/login
```
Body:
```json
{
  "email": "customer@example.com",
  "password": "password123"
}
```
Expected: `200 OK`. Copy the `accessToken`, click **Authorize** at the top of Swagger, paste the token, and click **Authorize**.

### Step 2: Add Product to Cart
Make sure you have a product in your catalog with sufficient stock.
```http
POST /api/cart/items
```
Body:
```json
{
  "productId": "<productId>",
  "quantity": 1
}
```
Expected: `201 Created`.

### Step 3: Checkout with MoMo Payment Method
Create an order. The `paymentMethod` MUST be `MOMO`.
```http
POST /api/orders/checkout
```
Body:
```json
{
  "addressId": "<addressId>",
  "paymentMethod": "MOMO",
  "shippingFee": 15000
}
```
Expected: `200 OK`.
Copy the `id` of the created order from the response body. This is your `<orderId>`. Note that `paymentStatus` is initially `UNPAID` and `status` is `PENDING`.

### Step 4: Initiate MoMo Payment
Generate the MoMo transaction payment URL.
```http
POST /api/payments/momo/initiate
```
Body:
```json
{
  "orderId": "<orderId>",
  "redirectUrl": "http://localhost:8080/api/payments/momo/redirect"
}
```
Expected: `200 OK`. Response body contains:
* `payUrl`: The URL to redirect the user to the MoMo Sandbox page.

Copy the value of `payUrl`.

### Step 5: Simulate Payment on Browser
1. Paste the copied `payUrl` into a new tab in your browser.
2. You will be redirected to the MoMo Sandbox payment page.
3. Click the sandbox success button if it is available.
4. MoMo will automatically:
   - Call the backend webhook (`POST /api/payments/momo-ipn`) in the background to update the order status to `PAID`.
   - Redirect your browser tab to the backend redirect endpoint: `http://localhost:8080/api/payments/momo/redirect`.
5. Your browser will show a plain HTML page with the MoMo result.

Important local testing note: if your backend is running only on `localhost`, MoMo cannot reach the IPN endpoint from the internet. Use ngrok and set `MOMO_IPN_URL` to the public URL if you want MoMo to update the order automatically. The redirect page is informational only; it does not mark the order as paid.

### Step 6: Verify Order Status
Go back to Swagger and query your order:
```http
GET /api/orders/{orderId}
```
Expected: `200 OK`. 
Confirm that `paymentStatus` has changed to **`PAID`**.

## 12. VNPay Payment Module (Sandbox - Backend Only)

### Step 1: Checkout with VNPay Payment Method

Create an order with `paymentMethod` set to `VNPAY`.

```http
POST /api/orders/checkout
```

```json
{
  "addressId": "<addressId>",
  "paymentMethod": "VNPAY",
  "shippingFee": 15000
}
```

Expected: `200 OK`. Copy the returned `id` as `<orderId>`.

### Step 2: Initiate VNPay Payment

```http
POST /api/payments/vnpay/initiate
```

```json
{
  "orderId": "<orderId>",
  "returnUrl": "http://localhost:8080/api/payments/vnpay/redirect"
}
```

Expected: `200 OK`. Copy `paymentUrl` and open it in your browser.

### Step 3: Pay in VNPay Sandbox

Use the VNPay sandbox payment page and complete the payment. After VNPay redirects back, the backend verifies the signed return parameters and updates the order status. For a real IPN callback, expose `/api/payments/vnpay-ipn` with ngrok and configure the public URL in the VNPay sandbox merchant settings.

### Step 4: Verify Order Status

```http
GET /api/orders/{orderId}
```

Expected after successful payment: `paymentStatus` is `PAID`.

## Notes

- Register creates `CUSTOMER` users only.
- Local admin bootstrap defaults to `admin@example.com` / `admin123456`. Override with `APP_ADMIN_EMAIL` and `APP_ADMIN_PASSWORD`.
- Product/category/warehouse/inventory `GET` endpoints are public. Mutation endpoints require `ADMIN`.

