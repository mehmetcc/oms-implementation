# API Endpoints Overview

This document outlines the available endpoints for the Authentication, Order, and Account services, along with a brief explanation of each endpoint's functionality.

---

## Authentication Service (resides in port 668)

- **POST /register**  
  Registers a new user. The request payload should include the username, password, and role. A successful registration returns a confirmation message.

- **POST /login**  
  Authenticates a user using provided credentials (username and password). On success, it returns a JWT token used for authenticating subsequent requests.

- **GET /users**

  Retrieves a list of all registered users.

---

## Order Service (resides in port 666)

- **POST /api/v1/orders**  
  Creates a new order.
    - **Admin users** can create orders on behalf of any customer.
    - **Regular users** are restricted to creating orders for themselves.

- **GET /api/v1/orders**  
  Retrieves a list of orders.
    - **Admin users** can query orders for any customer by providing a `customerId` parameter.
    - **Regular users** are limited to viewing only their own orders (if a `customerId` is provided, it must match the authenticated user's identifier).

- **DELETE /api/v1/orders**  
  Deletes an order specified by an `orderId` parameter.
    - **Admin users** can delete any order.
    - **Regular users** can delete only the orders that belong to them.

---

## Account Service (resides in port 667)

- **POST /api/v1/assets**  
  Creates a new asset entry.
    - **Admin users** can create assets for any customer.
    - **Regular users** are allowed to create assets only for their own account.

- **GET /api/v1/assets**  
  Lists asset entries.
    - **Admin users** have access to view assets for any customer.
    - **Regular users** can only retrieve asset data associated with their own customer identifier.

---
