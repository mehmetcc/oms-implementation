import React, { useContext, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../App';
import { Container, TextField, Button, Typography, Box, MenuItem } from '@mui/material';

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
    <Container maxWidth="sm">
      <Box sx={{ my: 4, p: 3, boxShadow: 3, borderRadius: 2 }}>
        <Typography variant="h4" align="center" gutterBottom>
          Create Order
        </Typography>
        <form onSubmit={handleSubmit}>
          {role === 'admin' && (
            <TextField
              fullWidth
              label="Customer ID"
              margin="normal"
              value={form.customerId}
              onChange={(e) => setForm({ ...form, customerId: e.target.value })}
            />
          )}
          <TextField
            fullWidth
            label="Asset Name"
            margin="normal"
            value={form.assetName}
            onChange={(e) => setForm({ ...form, assetName: e.target.value })}
          />
          <TextField
            fullWidth
            select
            label="Order Side"
            margin="normal"
            value={form.orderSide}
            onChange={(e) => setForm({ ...form, orderSide: e.target.value })}
          >
            <MenuItem value="BUY">BUY</MenuItem>
            <MenuItem value="SELL">SELL</MenuItem>
          </TextField>
          <TextField
            fullWidth
            label="Size"
            type="number"
            margin="normal"
            value={form.size}
            onChange={(e) => setForm({ ...form, size: Number(e.target.value) })}
          />
          <TextField
            fullWidth
            label="Price"
            type="number"
            margin="normal"
            value={form.price}
            onChange={(e) => setForm({ ...form, price: Number(e.target.value) })}
          />
          <Box sx={{ mt: 2, display: 'flex', justifyContent: 'space-between' }}>
            <Button variant="contained" color="secondary" onClick={() => navigate('/dashboard')}>
              Return
            </Button>
            <Button variant="contained" color="primary" type="submit">
              Create Order
            </Button>
          </Box>
        </form>
      </Box>
    </Container>
  );
}

export default OrderCreatePage;
