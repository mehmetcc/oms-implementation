import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, TextField, Button, Typography, Box, MenuItem } from '@mui/material';

const AUTH_URL = 'http://localhost:668';

function RegisterPage() {
  const [form, setForm] = useState({ username: '', password: '', role: 'customer' });
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    const res = await fetch(`${AUTH_URL}/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(form)
    });
    const data = await res.json();
    if (res.ok) {
      alert(data.message);
      navigate('/login');
    } else {
      alert(`Registration failed: ${data.error}`);
    }
  };

  return (
    <Container maxWidth="sm">
      <Box sx={{ my: 4, p: 3, boxShadow: 3, borderRadius: 2 }}>
        <Typography variant="h4" align="center" gutterBottom>
          Register
        </Typography>
        <form onSubmit={handleSubmit}>
          <TextField
            fullWidth
            label="Username"
            margin="normal"
            value={form.username}
            onChange={(e) => setForm({ ...form, username: e.target.value })}
          />
          <TextField
            fullWidth
            label="Password"
            type="password"
            margin="normal"
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
          />
          <TextField
            fullWidth
            select
            label="Role"
            margin="normal"
            value={form.role}
            onChange={(e) => setForm({ ...form, role: e.target.value })}
          >
            <MenuItem value="customer">Customer</MenuItem>
            <MenuItem value="admin">Admin</MenuItem>
          </TextField>
          <Box sx={{ mt: 2, textAlign: 'center' }}>
            <Button variant="contained" color="primary" type="submit">
              Register
            </Button>
          </Box>
        </form>
      </Box>
    </Container>
  );
}

export default RegisterPage;
