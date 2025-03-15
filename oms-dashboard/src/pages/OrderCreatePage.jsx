import React, { useContext, useState } from 'react';
import { AuthContext } from '../App';
import { useNavigate } from 'react-router-dom';

const ORDER_URL = 'http://localhost:666/api/v1/orders';

function OrderCreatePage() {
  const { auth } = useContext(AuthContext);
  const navigate = useNavigate();
  const role = JSON.parse(atob(auth.token.split('.')[1])).role;
  const [form, setForm] = useState({
    customerId: role === 'admin' ? '' : auth.user,
    assetName: '',
    orderSide: 'BUY',
    size: 0,
    price: 0,
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    const res = await fetch(ORDER_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${auth.token}`
      },
      body: JSON.stringify(form)
    });
    const data = await res.json();
    if (res.ok) {
      alert(`Order created with ID: ${data.id}`);
      navigate('/orders');
    } else {
      alert(`Failed to create order: ${data.error}`);
    }
  };

  return (
    <div>
      <h2>Create Order</h2>
      <form onSubmit={handleSubmit}>
        {role === 'admin' && (
          <label>
            CustomerId:
            <input
              value={form.customerId}
              onChange={e => setForm({ ...form, customerId: e.target.value })}
            />
          </label>
        )}
        <br/>
        <label>
          Asset Name:
          <input
            value={form.assetName}
            onChange={e => setForm({ ...form, assetName: e.target.value })}
          />
        </label>
        <br/>
        <label>
          Order Side:
          <select
            value={form.orderSide}
            onChange={e => setForm({ ...form, orderSide: e.target.value })}
          >
            <option value="BUY">BUY</option>
            <option value="SELL">SELL</option>
          </select>
        </label>
        <br/>
        <label>
          Size:
          <input
            type="number"
            value={form.size}
            onChange={e => setForm({ ...form, size: Number(e.target.value) })}
          />
        </label>
        <br/>
        <label>
          Price:
          <input
            type="number"
            value={form.price}
            onChange={e => setForm({ ...form, price: Number(e.target.value) })}
          />
        </label>
        <br/>
        <button type="submit">Create Order</button>
      </form>
    </div>
  );
}

export default OrderCreatePage;
