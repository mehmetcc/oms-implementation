import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardHome from './pages/DashboardHome';
import ListUsersPage from './pages/ListUsersPage';
import OrdersPage from './pages/OrdersPage';
import OrderCreatePage from './pages/OrderCreatePage';
import AssetsPage from './pages/AssetsPage';
import AssetCreatePage from './pages/AssetCreatePage';

export const AuthContext = React.createContext(null);

function App() {
  const [auth, setAuth] = useState({ token: null, user: null });
  return (
    <AuthContext.Provider value={{ auth, setAuth }}>
      <Router>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/dashboard" element={auth.token ? <DashboardHome /> : <Navigate to="/login" />} />
          <Route path="/orders" element={auth.token ? <OrdersPage /> : <Navigate to="/login" />} />
          <Route path="/create-order" element={auth.token ? <OrderCreatePage /> : <Navigate to="/login" />} />
          <Route path="/assets" element={auth.token ? <AssetsPage /> : <Navigate to="/login" />} />
          <Route path="/users-list" element={auth.token ? <ListUsersPage /> : <Navigate to="/login" />} />
          <Route path="/create-asset" element={auth.token ? <AssetCreatePage /> : <Navigate to="/login" />} />
          <Route path="*" element={<Navigate to="/login" />} />
        </Routes>
      </Router>
    </AuthContext.Provider>
  );
}

export default App;
