import React, { useEffect, useState, useContext } from 'react';
import { Container, Typography, Box, List, ListItem, ListItemText, Button } from '@mui/material';
import { AuthContext } from '../App';
import { useNavigate } from 'react-router-dom';

const USERS_URL = 'http://localhost:668/users';

function ListUsersPage() {
  const { auth } = useContext(AuthContext);
  const [users, setUsers] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    if (!auth.token) {
      console.error('No auth token found');
      return;
    }

    const fetchUsers = async () => {
      try {
        const res = await fetch(USERS_URL);
        const data = await res.json();
        if (res.ok) {
          setUsers(data.users);
        } else {
          console.error('Error fetching users:', data.error);
          alert(data.error || 'Failed to fetch users');
        }
      } catch (error) {
        console.error('Fetch error:', error);
        alert('An error occurred while fetching users');
      }
    };
    fetchUsers();
  }, [auth.token]);

  return (
    <Container maxWidth="sm">
      <Box sx={{ my: 4, p: 3, boxShadow: 3, borderRadius: 2 }}>
        <Typography variant="h4" align="center" gutterBottom>
          Users List
        </Typography>
        <List>
          {users.map(user => (
            <ListItem key={user.id}>
              <ListItemText
                primary={`Username: ${user.username}`}
                secondary={`Role: ${user.role} | User ID: ${user.id}`}
              />
            </ListItem>
          ))}
        </List>
        <Box sx={{ textAlign: 'center', mt: 2 }}>
          <Button
            variant="contained"
            color="secondary"
            onClick={() => navigate('/dashboard')}
            sx={{ width: '150px', height: '50px' }}
          >
            Return
          </Button>
        </Box>
      </Box>
    </Container>
  );
}

export default ListUsersPage;
