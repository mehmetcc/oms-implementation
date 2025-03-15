import React, { useContext, useState } from 'react';
import { AuthContext } from '../App';
import { useNavigate } from 'react-router-dom';
import { Container, TextField, Button, Typography, Box } from '@mui/material';

const ASSET_URL = 'http://localhost:667/api/v1/assets';

function AssetCreatePage() {
  const { auth } = useContext(AuthContext);
  const navigate = useNavigate();
  const role = JSON.parse(atob(auth.token.split('.')[1])).role;
  const [form, setForm] = useState({
    customerId: role === 'admin' ? '' : auth.user,
    assetName: '',
    totalSize: 0,
    usableSize: 0,
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    const res = await fetch(ASSET_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${auth.token}`
      },
      body: JSON.stringify(form)
    });
    const data = await res.json();
    if (res.ok) {
      alert(`Asset created with ID: ${data.id}`);
      navigate('/assets');
    } else {
      alert(`Failed to create asset: ${data.error}`);
    }
  };

  return (
    <Container maxWidth="sm">
      <Box sx={{ my: 4, p: 3, boxShadow: 3, borderRadius: 2 }}>
        <Typography variant="h4" align="center" gutterBottom>
          Create Asset
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
            label="Total Size"
            type="number"
            margin="normal"
            value={form.totalSize}
            onChange={(e) => setForm({ ...form, totalSize: Number(e.target.value) })}
          />
          <TextField
            fullWidth
            label="Usable Size"
            type="number"
            margin="normal"
            value={form.usableSize}
            onChange={(e) => setForm({ ...form, usableSize: Number(e.target.value) })}
          />
          <Box sx={{ mt: 2, textAlign: 'center' }}>
            <Button variant="contained" color="primary" type="submit">
              Create Asset
            </Button>
          </Box>
        </form>
      </Box>
    </Container>
  );
}

export default AssetCreatePage;
