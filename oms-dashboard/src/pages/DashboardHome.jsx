import React, { useContext } from 'react';
import { AuthContext } from '../App';
import { Link as RouterLink } from 'react-router-dom';
import { Container, Typography, Box, Button, Stack } from '@mui/material';

function DashboardHome() {
  const { auth } = useContext(AuthContext);
  const role = auth.token ? JSON.parse(atob(auth.token.split('.')[1])).role : '';
  const buttonStyles = {
    width: '150px',
    height: '50px',
    fontSize: '1rem',
  };

  return (
    <Container maxWidth="sm">
      <Box sx={{ my: 4, p: 3, boxShadow: 3, borderRadius: 2, textAlign: 'center' }}>
        <Typography variant="h4" gutterBottom>
          Dashboard Home
        </Typography>
        <Typography variant="subtitle1" gutterBottom>
          Welcome, {auth.user}! [Role: {role}]
        </Typography>
        <Stack direction="row" spacing={2} justifyContent="center" sx={{ mt: 2 }}>
          <Button variant="contained" component={RouterLink} to="/orders" sx={buttonStyles}>
            Orders
          </Button>
          <Button variant="contained" component={RouterLink} to="/create-order" sx={buttonStyles}>
            Create Order
          </Button>
          <Button variant="contained" component={RouterLink} to="/assets" sx={buttonStyles}>
            Assets
          </Button>
          <Button variant="contained" component={RouterLink} to="/create-asset" sx={buttonStyles}>
            Create Asset
          </Button>
          <Button variant="contained" component={RouterLink} to="/users-list" sx={buttonStyles}>
            Users
          </Button>
        </Stack>
      </Box>
    </Container>
  );
}

export default DashboardHome;
