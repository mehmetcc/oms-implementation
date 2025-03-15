import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../App';
import { useNavigate } from 'react-router-dom';
import { Container, TextField, Button, Typography, Box, List, ListItem, ListItemText } from '@mui/material';

const ORDER_URL = 'http://localhost:666/api/v1/orders';

function OrdersPage() {
  const { auth } = useContext(AuthContext);
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [filterCustomerId, setFilterCustomerId] = useState('');
  const role = JSON.parse(atob(auth.token.split('.')[1])).role;

  const fetchOrders = async () => {
    const url = filterCustomerId ? `${ORDER_URL}?customerId=${filterCustomerId}` : ORDER_URL;
    const res = await fetch(url, {
      headers: { 'Authorization': `Bearer ${auth.token}` }
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
      headers: { 'Authorization': `Bearer ${auth.token}` }
    });
    if (res.ok) {
      const data = await res.json();
      // Extract and display the message from the API response.
      alert(data.message);
      fetchOrders();
    } else {
      alert('Failed to delete order');
    }
  };

  return (
    <Container maxWidth="sm">
      <Box sx={{ my: 4, p: 3, boxShadow: 3, borderRadius: 2 }}>
        <Typography variant="h4" align="center" gutterBottom>
          Orders
        </Typography>
        {role === 'admin' && (
          <TextField
            fullWidth
            label="Filter by Customer ID"
            margin="normal"
            value={filterCustomerId}
            onChange={(e) => setFilterCustomerId(e.target.value)}
          />
        )}
        <Box sx={{ textAlign: 'center', mb: 2 }}>
          <Button variant="contained" color="primary" onClick={fetchOrders}>
            Refresh
          </Button>
        </Box>
        <List>
          {orders.map(order => (
            <ListItem key={order.id} sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <ListItemText
                primary={`Order: ${order.id} - ${order.orderSide} - Customer: ${order.customerId} - Price: ${order.price} - Status: ${order.status}`}
              />
              <Button
                variant="outlined"
                color="secondary"
                onClick={() => handleDelete(order.id)}
              >
                Delete
              </Button>
            </ListItem>
          ))}
        </List>
        <Box sx={{ textAlign: 'center', mt: 2 }}>
          <Button variant="contained" color="secondary" onClick={() => navigate('/dashboard')}>
            Return
          </Button>
        </Box>
      </Box>
    </Container>
  );
}

export default OrdersPage;
