// STUB: generated

const BASE_AUTH_URL = "http://localhost:668";
const BASE_ORDER_URL = "http://localhost:666/api/v1/orders";

// Helper function for sending JSON requests
async function sendRequest(url, method, body, token = null) {
  const headers = { "Content-Type": "application/json" };
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }
  const res = await fetch(url, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  const contentType = res.headers.get("content-type") || "";
  let data;
  if (contentType.includes("application/json")) {
    data = await res.json();
  } else {
    data = await res.text();
  }
  return { status: res.status, data };
}

// Authentication Functions
async function registerUser(username, password, role) {
  const { status, data } = await sendRequest(
    `${BASE_AUTH_URL}/register`,
    "POST",
    { username, password, role }
  );
  console.log(
    `Register ${username} (${role}): status ${status}, response: ${JSON.stringify(data)}`
  );
}

async function loginUser(username, password) {
  const { status, data } = await sendRequest(
    `${BASE_AUTH_URL}/login`,
    "POST",
    { username, password }
  );
  if (status === 200) {
    console.log(`Login ${username}: status ${status}, token: ${data.token}`);
    return data.token;
  } else {
    console.error(
      `Login ${username} failed: status ${status}, response: ${JSON.stringify(data)}`
    );
    return null;
  }
}

// Order Functions
async function createOrder(token, customerId, assetName, orderSide, size, price) {
  const payload = {
    customerId,
    assetName,
    orderSide, // "BUY" or "SELL" – ensure this matches your OrderSide enum.
    size,
    price
  };
  const { status, data } = await sendRequest(BASE_ORDER_URL, "POST", payload, token);
  console.log(`Create order (customerId=${customerId}, assetName=${assetName}, orderSide=${orderSide}, size=${size}, price=${price}): status ${status}, response: ${JSON.stringify(data)}`);
  return data;
}

async function listOrders(token, params = {}) {
  const query = new URLSearchParams(params).toString();
  const url = BASE_ORDER_URL + (query ? "?" + query : "");
  const { status, data } = await sendRequest(url, "GET", null, token);
  console.log(`List orders (params=${JSON.stringify(params)}): status ${status}, response: ${JSON.stringify(data)}`);
  return data;
}

async function deleteOrder(token, orderId) {
  const url = BASE_ORDER_URL + "?orderId=" + orderId;
  const { status, data } = await sendRequest(url, "DELETE", null, token);
  console.log(`Delete order (orderId=${orderId}): status ${status}, response: ${JSON.stringify(data)}`);
  return data;
}

(async function runTests() {
  console.log("=== AUTH SERVICE TESTS ===");
  await registerUser("adminUser100", "adminpass", "admin");
  await registerUser("user1", "userpass", "customer");

  const adminToken = await loginUser("adminUser100", "adminpass");
  const userToken = await loginUser("user1", "userpass");

  if (!adminToken || !userToken) {
    console.error("Login failed for one or more users. Aborting tests.");
    return;
  }

  console.log("\n=== ORDER SERVICE TESTS ===");
  // 1. Admin creates an order for user1.
  const adminOrder = await createOrder(adminToken, "user1", "AAPL", "BUY", 10, 150);
  const adminOrderId = adminOrder?.id;

  // 2. User creates an order for themselves.
  const userOrder = await createOrder(userToken, "user1", "GOOG", "SELL", 5, 2800);
  const userOrderId = userOrder?.id;

  // 3. User attempts to create an order for a different customer (should be forbidden).
  await createOrder(userToken, "otherUser", "MSFT", "BUY", 8, 300);

  // 4. Admin lists orders for user1.
  await listOrders(adminToken, { customerId: "user1" });

  // 5. User lists their own orders.
  await listOrders(userToken);

  // 6. Admin deletes an order.
  if (adminOrderId) {
    await deleteOrder(adminToken, adminOrderId);
  } else {
    console.error("No admin order available to delete.");
  }

  // 7. User deletes their own order.
  if (userOrderId) {
    await deleteOrder(userToken, userOrderId);
  } else {
    console.error("No user order available to delete.");
  }

  // 8. User attempts to delete an order not belonging to them.
  // First, admin creates an order for "anotherUser".
  const otherOrder = await createOrder(adminToken, "anotherUser", "TSLA", "BUY", 12, 700);
  const otherOrderId = otherOrder?.id;
  if (otherOrderId) {
    await deleteOrder(userToken, otherOrderId);
  } else {
    console.error("No order available for forbidden deletion test.");
  }
})();
