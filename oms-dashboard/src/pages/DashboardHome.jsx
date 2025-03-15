import React, { useContext } from 'react';
import { AuthContext } from '../App';
import { Link } from 'react-router-dom';

function DashboardHome() {
  const { auth } = useContext(AuthContext);
  const role = auth.token ? JSON.parse(atob(auth.token.split('.')[1])).role : '';
  return (
    <div>
      <h2>Dashboard Home</h2>
      <p>Welcome, {auth.user}! [Role: {role}]</p>
      <nav>
        <ul>
          <li><Link to="/orders">Orders</Link></li>
          <li><Link to="/create-order">Create Order</Link></li>
          <li><Link to="/assets">Assets</Link></li>
          <li><Link to="/create-asset">Create Asset</Link></li>
        </ul>
      </nav>
    </div>
  );
}

export default DashboardHome;
