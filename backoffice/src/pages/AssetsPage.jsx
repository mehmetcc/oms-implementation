import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../App';
import { useNavigate } from 'react-router-dom';
import { Container, TextField, Button, Typography, Box, List, ListItem, ListItemText } from '@mui/material';

const ASSET_URL = 'http://localhost:667/api/v1/assets';
const DELETE_ORDER_URL = 'http://localhost:667/api/v1/orders/';

function AssetsPage() {
  const { auth } = useContext(AuthContext);
  const navigate = useNavigate();
  const [assets, setAssets] = useState([]);
  const role = JSON.parse(atob(auth.token.split('.')[1])).role;
  const [filterCustomerId, setFilterCustomerId] = useState('');

  const fetchAssets = async () => {
    const url = filterCustomerId ? `${ASSET_URL}?customerId=${filterCustomerId}` : ASSET_URL;
    const res = await fetch(url, {
      headers: { 'Authorization': `Bearer ${auth.token}` }
    });
    const data = await res.json();
    if (res.ok) {
      setAssets(data.assets);
    } else {
      alert('Failed to fetch assets');
    }
  };

  const handleDelete = async (orderId) => {
    const res = await fetch(`${DELETE_ORDER_URL}${orderId}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${auth.token}`,
        'Content-Type': 'application/json'
      }
    });
    const data = await res.json();
    if (res.ok) {
      // Display only the message from the API response.
      alert(data.message);
    } else {
      alert('Failed to delete order');
    }
  };

  useEffect(() => {
    fetchAssets();
  }, [filterCustomerId]);

  return (
    <Container maxWidth="sm">
      <Box sx={{ my: 4, p: 3, boxShadow: 3, borderRadius: 2 }}>
        <Typography variant="h4" align="center" gutterBottom>
          Assets
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
          <Button variant="contained" color="primary" onClick={fetchAssets}>
            Refresh
          </Button>
        </Box>
        <List>
          {assets.map(asset => (
            <ListItem key={asset.assetName}>
              <ListItemText
                primary={asset.assetName}
                secondary={`Total: ${asset.totalSize} - Customer: ${asset.customerId} - Usable: ${asset.usableSize}`}
              />
              <Button
                variant="contained"
                color="error"
                onClick={() => handleDelete(asset.id)} // Assuming asset.id is the identifier needed.
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

export default AssetsPage;
