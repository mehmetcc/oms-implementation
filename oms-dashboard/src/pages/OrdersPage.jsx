import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../App';

const ORDER_URL = 'http://localhost:666/api/v1/orders';

function OrdersPage() {
  const { auth } = useContext(AuthContext);
  const [orders, setOrders] = useState([]);
  const [filterCustomerId, setFilterCustomerId] = useState('');

  const fetchOrders = async () => {
    const url = filterCustomerId ? `${ORDER_URL}?customerId=${filterCustomerId}` : ORDER_URL;
    const res = await fetch(url, {
      headers: {
        'Authorization': `Bearer ${auth.token}`
      }
    });
    const data = await res.json();
    if (res.ok) {
      setOrders(data.orders);
    } else {
      alert('Failed to fetch orders');
    }
  };

  useEffect(() => { fetchOrders(); }, [filterCustomerId]);

  const handleDelete = async (orderId) => {
    const res = await fetch(`${ORDER_URL}?orderId=${orderId}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${auth.token}`
      }
    });
    if (res.ok) {
      alert('Order deleted');
      fetchOrders();
    } else {
      alert('Failed to delete order');
    }
  };

  const role = JSON.parse(atob(auth.token.split('.')[1])).role;

  return (
    <div>
      <h2>Orders</h2>
      {role === 'admin' && (
        <div>
          <label>
            Filter by CustomerId:
            <input
              value={filterCustomerId}
              onChange={e => setFilterCustomerId(e.target.value)}
            />
          </label>
        </div>
      )}
      <button onClick={fetchOrders}>Refresh</button>
      <ul>
        {orders.map(order => (
          <li key={order.id}>
            Order: {order.id} - {order.orderSide} - Price: {order.price} - Status: {order.status}
            <button onClick={() => handleDelete(order.id)}>Delete</button>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default OrdersPage;
