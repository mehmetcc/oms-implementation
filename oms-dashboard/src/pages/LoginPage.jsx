import React, { useContext, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../App';
import { Container, TextField, Button, Typography, Box, Link } from '@mui/material';

const AUTH_URL = 'http://localhost:668';

function LoginPage() {
  const [credentials, setCredentials] = useState({ username: '', password: '' });
  const { setAuth } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    const res = await fetch(`${AUTH_URL}/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(credentials)
    });
    const data = await res.json();
    if (res.ok) {
      setAuth({ token: data.token, user: credentials.username });
      navigate('/dashboard');
    } else {
      alert(`Login failed: ${data.error}`);
    }
  };

  return (
    <Container maxWidth="sm">
      <Box sx={{ my: 4, p: 3, boxShadow: 3, borderRadius: 2 }}>
        <Typography variant="h4" align="center" gutterBottom>
          Login
        </Typography>
        <form onSubmit={handleSubmit}>
          <TextField
            fullWidth
            label="Username"
            margin="normal"
            value={credentials.username}
            onChange={(e) => setCredentials({ ...credentials, username: e.target.value })}
          />
          <TextField
            fullWidth
            label="Password"
            type="password"
            margin="normal"
            value={credentials.password}
            onChange={(e) => setCredentials({ ...credentials, password: e.target.value })}
          />
          <Box sx={{ mt: 2, textAlign: 'center' }}>
            <Button variant="contained" color="primary" type="submit">
              Login
            </Button>
          </Box>
        </form>
        <Typography align="center" sx={{ mt: 2 }}>
          Don't have an account?{' '}
          <Link href="/register">Register here</Link>
        </Typography>
      </Box>
    </Container>
  );
}

export default LoginPage;
