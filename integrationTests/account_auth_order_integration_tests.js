// integration-test.js
// Run this file with Node v18+ (which has global fetch)
// This script tests the Authentication, Order, and Account endpoints.

import assert from 'node:assert';

// Base URLs for the three services
const AUTH_URL = 'http://localhost:668';
const ORDER_URL = 'http://localhost:666/api/v1/orders';
const ASSET_URL = 'http://localhost:667/api/v1/assets';

// Helper: call a JSON API
async function callApi(url, method, token, bodyObj) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = `Bearer ${token}`;
  const response = await fetch(url, {
    method,
    headers,
    body: bodyObj ? JSON.stringify(bodyObj) : undefined,
  });
  const result = await response.json().catch(() => ({}));
  return { status: response.status, body: result };
}

async function runTests() {
  console.log('Starting integration tests...\n');

  // --------- CUSTOMER FLOW ---------

  // 1. Register customer
  const customerUser = { username: 'customer1', password: 'password1', role: 'customer' };
  let res = await callApi(`${AUTH_URL}/register`, 'POST', null, customerUser);
  console.log('Customer register:', res);
  assert.strictEqual(res.status, 201, 'Customer registration should return 201');

  // 2. Login customer
  res = await callApi(`${AUTH_URL}/login`, 'POST', null, {
    username: customerUser.username,
    password: customerUser.password,
  });
  console.log('Customer login:', res);
  assert.strictEqual(res.status, 200, 'Customer login should return 200');
  const customerToken = res.body.token;
  assert.ok(customerToken, 'Customer login must return a token');

  // 3. Deposit asset (TRY) for buying
  const assetPayload = {
    customerId: customerUser.username, // using username as customerId
    assetName: 'TRY',
    totalSize: 1000,
    usableSize: 1000,
  };
  res = await callApi(`${ASSET_URL}`, 'POST', customerToken, assetPayload);
  console.log('Customer asset deposit:', res);
  assert.strictEqual(res.status, 200, 'Asset deposit should return 200');
  const assetId = res.body.id;
  assert.ok(assetId, 'Asset creation should return an asset id');

  // 4. List assets (for customer)
  res = await callApi(`${ASSET_URL}?customerId=${customerUser.username}`, 'GET', customerToken);
  console.log('Customer asset list:', res);
  assert.strictEqual(res.status, 200, 'Asset list should return 200');
  assert.ok(Array.isArray(res.body.assets), 'Asset list response should include an array of assets');

  // 5. Create order (BUY order) for customer
  const orderPayload = {
    customerId: customerUser.username,
    assetName: 'TRY',
    orderSide: 'BUY',
    size: 100,
    price: 10,
  };
  res = await callApi(`${ORDER_URL}`, 'POST', customerToken, orderPayload);
  console.log('Customer create order:', res);
  assert.strictEqual(res.status, 200, 'Order creation should return 200');
  const orderId = res.body.id;
  assert.ok(orderId, 'Order creation should return an order id');

  // 6. List orders for customer
  res = await callApi(`${ORDER_URL}?customerId=${customerUser.username}`, 'GET', customerToken);
  console.log('Customer order list:', res);
  assert.strictEqual(res.status, 200, 'Order list should return 200');
  assert.ok(Array.isArray(res.body.orders), 'Order list response should include an array of orders');
  const foundOrder = res.body.orders.find((o) => o.id === orderId);
  assert.ok(foundOrder, 'Created order should be found in the order list');

  // 7. Delete the order as customer
  res = await callApi(`${ORDER_URL}?orderId=${orderId}`, 'DELETE', customerToken);
  console.log('Customer delete order:', res);
  assert.strictEqual(res.status, 200, 'Order deletion should return 200');

  // --------- ADMIN FLOW ---------

  // 8. Register admin
  const adminUser = { username: 'admin1', password: 'adminpass', role: 'admin' };
  res = await callApi(`${AUTH_URL}/register`, 'POST', null, adminUser);
  console.log('Admin register:', res);
  assert.strictEqual(res.status, 201, 'Admin registration should return 201');

  // 9. Login admin
  res = await callApi(`${AUTH_URL}/login`, 'POST', null, {
    username: adminUser.username,
    password: adminUser.password,
  });
  console.log('Admin login:', res);
  assert.strictEqual(res.status, 200, 'Admin login should return 200');
  const adminToken = res.body.token;
  assert.ok(adminToken, 'Admin login must return a token');

  // 10. As admin, create an order for customer1 (on their behalf)
  const adminOrderPayload = {
    customerId: customerUser.username,
    assetName: 'TRY',
    orderSide: 'SELL',
    size: 50,
    price: 12,
  };
  res = await callApi(`${ORDER_URL}`, 'POST', adminToken, adminOrderPayload);
  console.log('Admin create order for customer1:', res);
  assert.strictEqual(res.status, 200, 'Admin order creation should return 200');
  const adminOrderId = res.body.id;
  assert.ok(adminOrderId, 'Admin order creation should return an order id');

  // 11. As admin, list orders for customer1 by providing customerId
  res = await callApi(`${ORDER_URL}?customerId=${customerUser.username}`, 'GET', adminToken);
  console.log('Admin list orders for customer1:', res);
  assert.strictEqual(res.status, 200, 'Admin order list should return 200');
  assert.ok(Array.isArray(res.body.orders), 'Order list should be an array');
  const foundAdminOrder = res.body.orders.find((o) => o.id === adminOrderId);
  assert.ok(foundAdminOrder, 'Admin-created order should be found in the order list');

  // 12. As admin, delete the admin-created order
  res = await callApi(`${ORDER_URL}?orderId=${adminOrderId}`, 'DELETE', adminToken);
  console.log('Admin delete order:', res);
  assert.strictEqual(res.status, 200, 'Admin order deletion should return 200');

  console.log('\nAll integration tests passed!');
}

runTests().catch((error) => {
  console.error('Integration test failed:', error);
  process.exit(1);
});
