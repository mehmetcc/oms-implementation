#!/bin/bash
set -e

PROJECT_NAME="oms-dashboard"
mkdir -p $PROJECT_NAME
cd $PROJECT_NAME
npm init -y

# Install runtime dependencies (React, ReactDOM, React Router DOM)
npm install react@18 react-dom@18 react-router-dom@6

# Install development dependencies
npm install -D webpack webpack-cli webpack-dev-server babel-loader @babel/core @babel/preset-env @babel/preset-react html-webpack-plugin

# Create directory structure
mkdir -p public src src/pages

# Create public/index.html
cat << 'EOF' > public/index.html
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>OMS Dashboard</title>
  </head>
  <body>
    <div id="root"></div>
  </body>
</html>
EOF

# Create src/index.jsx (entry point)
cat << 'EOF' > src/index.jsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);
EOF

# Create src/App.jsx
cat << 'EOF' > src/App.jsx
import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardHome from './pages/DashboardHome';
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
          <Route path="/create-asset" element={auth.token ? <AssetCreatePage /> : <Navigate to="/login" />} />
          <Route path="*" element={<Navigate to="/login" />} />
        </Routes>
      </Router>
    </AuthContext.Provider>
  );
}

export default App;
EOF

# Create pages/LoginPage.jsx
cat << 'EOF' > src/pages/LoginPage.jsx
import React, { useContext, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../App';

const AUTH_URL = 'http://localhost:668';

function LoginPage() {
  const [credentials, setCredentials] = useState({ username: '', password: '' });
  const { setAuth } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    const res = await fetch(\`\${AUTH_URL}/login\`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(credentials)
    });
    const data = await res.json();
    if (res.ok) {
      setAuth({ token: data.token, user: credentials.username });
      navigate('/dashboard');
    } else {
      alert(\`Login failed: \${data.error}\`);
    }
  };

  return (
    <div>
      <h2>Login</h2>
      <form onSubmit={handleSubmit}>
        <label>
          Username:
          <input
            value={credentials.username}
            onChange={e => setCredentials({ ...credentials, username: e.target.value })}
          />
        </label>
        <br/>
        <label>
          Password:
          <input
            type="password"
            value={credentials.password}
            onChange={e => setCredentials({ ...credentials, password: e.target.value })}
          />
        </label>
        <br/>
        <button type="submit">Login</button>
      </form>
      <p>Don't have an account? <a href="/register">Register here</a></p>
    </div>
  );
}

export default LoginPage;
EOF

# Create pages/RegisterPage.jsx
cat << 'EOF' > src/pages/RegisterPage.jsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const AUTH_URL = 'http://localhost:668';

function RegisterPage() {
  const [form, setForm] = useState({ username: '', password: '', role: 'customer' });
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    const res = await fetch(\`\${AUTH_URL}/register\`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(form)
    });
    const data = await res.json();
    if (res.ok) {
      alert(data.message);
      navigate('/login');
    } else {
      alert(\`Registration failed: \${data.error}\`);
    }
  };

  return (
    <div>
      <h2>Register</h2>
      <form onSubmit={handleSubmit}>
        <label>
          Username:
          <input value={form.username} onChange={e => setForm({ ...form, username: e.target.value })} />
        </label>
        <br/>
        <label>
          Password:
          <input type="password" value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} />
        </label>
        <br/>
        <label>
          Role:
          <select value={form.role} onChange={e => setForm({ ...form, role: e.target.value })}>
            <option value="customer">Customer</option>
            <option value="admin">Admin</option>
          </select>
        </label>
        <br/>
        <button type="submit">Register</button>
      </form>
    </div>
  );
}

export default RegisterPage;
EOF

# Create pages/DashboardHome.jsx
cat << 'EOF' > src/pages/DashboardHome.jsx
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
EOF

# Create pages/OrdersPage.jsx
cat << 'EOF' > src/pages/OrdersPage.jsx
import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../App';

const ORDER_URL = 'http://localhost:666/api/v1/orders';

function OrdersPage() {
  const { auth } = useContext(AuthContext);
  const [orders, setOrders] = useState([]);
  const [filterCustomerId, setFilterCustomerId] = useState('');

  const fetchOrders = async () => {
    const url = filterCustomerId ? \`\${ORDER_URL}?customerId=\${filterCustomerId}\` : ORDER_URL;
    const res = await fetch(url, {
      headers: { 'Authorization': \`Bearer \${auth.token}\` }
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
    const res = await fetch(\`\${ORDER_URL}?orderId=\${orderId}\`, {
      method: 'DELETE',
      headers: { 'Authorization': \`Bearer \${auth.token}\` }
    });
    if (res.ok) {
      alert('Order deleted');
      fetchOrders();
    } else {
      alert('Failed to delete order');
    }
  };

  const role = JSON.parse(atob(auth.token.split('.')[1])).role;

  return (
    <div>
      <h2>Orders</h2>
      {role === 'admin' && (
        <div>
          <label>
            Filter by CustomerId:
            <input value={filterCustomerId} onChange={e => setFilterCustomerId(e.target.value)} />
          </label>
        </div>
      )}
      <button onClick={fetchOrders}>Refresh</button>
      <ul>
        {orders.map(order => (
          <li key={order.id}>
            Order: {order.id} - {order.orderSide} - Price: {order.price}
            <button onClick={() => handleDelete(order.id)}>Delete</button>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default OrdersPage;
EOF

# Create pages/OrderCreatePage.jsx
cat << 'EOF' > src/pages/OrderCreatePage.jsx
import React, { useContext, useState } from 'react';
import { AuthContext } from '../App';
import { useNavigate } from 'react-router-dom';

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
      headers: { 'Content-Type': 'application/json', 'Authorization': \`Bearer \${auth.token}\` },
      body: JSON.stringify(form)
    });
    const data = await res.json();
    if (res.ok) {
      alert(\`Order created with ID: \${data.id}\`);
      navigate('/orders');
    } else {
      alert(\`Failed to create order: \${data.error}\`);
    }
  };

  return (
    <div>
      <h2>Create Order</h2>
      <form onSubmit={handleSubmit}>
        {role === 'admin' && (
          <label>
            CustomerId:
            <input value={form.customerId} onChange={e => setForm({ ...form, customerId: e.target.value })} />
          </label>
        )}
        <br/>
        <label>
          Asset Name:
          <input value={form.assetName} onChange={e => setForm({ ...form, assetName: e.target.value })} />
        </label>
        <br/>
        <label>
          Order Side:
          <select value={form.orderSide} onChange={e => setForm({ ...form, orderSide: e.target.value })}>
            <option value="BUY">BUY</option>
            <option value="SELL">SELL</option>
          </select>
        </label>
        <br/>
        <label>
          Size:
          <input type="number" value={form.size} onChange={e => setForm({ ...form, size: Number(e.target.value) })} />
        </label>
        <br/>
        <label>
          Price:
          <input type="number" value={form.price} onChange={e => setForm({ ...form, price: Number(e.target.value) })} />
        </label>
        <br/>
        <button type="submit">Create Order</button>
      </form>
    </div>
  );
}

export default OrderCreatePage;
EOF

# Create pages/AssetsPage.jsx
cat << 'EOF' > src/pages/AssetsPage.jsx
import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../App';

const ASSET_URL = 'http://localhost:667/api/v1/assets';

function AssetsPage() {
  const { auth } = useContext(AuthContext);
  const [assets, setAssets] = useState([]);
  const role = JSON.parse(atob(auth.token.split('.')[1])).role;
  const [filterCustomerId, setFilterCustomerId] = useState('');

  const fetchAssets = async () => {
    const url = filterCustomerId ? \`\${ASSET_URL}?customerId=\${filterCustomerId}\` : ASSET_URL;
    const res = await fetch(url, {
      headers: { 'Authorization': \`Bearer \${auth.token}\` }
    });
    const data = await res.json();
    if (res.ok) {
      setAssets(data.assets);
    } else {
      alert('Failed to fetch assets');
    }
  };

  useEffect(() => { fetchAssets(); }, [filterCustomerId]);

  return (
    <div>
      <h2>Assets</h2>
      {role === 'admin' && (
        <div>
          <label>
            Filter by CustomerId:
            <input value={filterCustomerId} onChange={e => setFilterCustomerId(e.target.value)} />
          </label>
        </div>
      )}
      <button onClick={fetchAssets}>Refresh</button>
      <ul>
        {assets.map(asset => (
          <li key={asset.assetName}>
            {asset.assetName} - Total: {asset.totalSize} - Usable: {asset.usableSize}
          </li>
        ))}
      </ul>
    </div>
  );
}

export default AssetsPage;
EOF

# Create pages/AssetCreatePage.jsx
cat << 'EOF' > src/pages/AssetCreatePage.jsx
import React, { useContext, useState } from 'react';
import { AuthContext } from '../App';
import { useNavigate } from 'react-router-dom';

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
      headers: { 'Content-Type': 'application/json', 'Authorization': \`Bearer \${auth.token}\` },
      body: JSON.stringify(form)
    });
    const data = await res.json();
    if (res.ok) {
      alert(\`Asset created with ID: \${data.id}\`);
      navigate('/assets');
    } else {
      alert(\`Failed to create asset: \${data.error}\`);
    }
  };

  return (
    <div>
      <h2>Create Asset</h2>
      <form onSubmit={handleSubmit}>
        {role === 'admin' && (
          <label>
            CustomerId:
            <input value={form.customerId} onChange={e => setForm({ ...form, customerId: e.target.value })} />
          </label>
        )}
        <br/>
        <label>
          Asset Name:
          <input value={form.assetName} onChange={e => setForm({ ...form, assetName: e.target.value })} />
        </label>
        <br/>
        <label>
          Total Size:
          <input type="number" value={form.totalSize} onChange={e => setForm({ ...form, totalSize: Number(e.target.value) })} />
        </label>
        <br/>
        <label>
          Usable Size:
          <input type="number" value={form.usableSize} onChange={e => setForm({ ...form, usableSize: Number(e.target.value) })} />
        </label>
        <br/>
        <button type="submit">Create Asset</button>
      </form>
    </div>
  );
}

export default AssetCreatePage;
EOF

# Create webpack.config.js in the project root
cat << 'EOF' > webpack.config.js
const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
  entry: './src/index.jsx',
  output: {
    path: path.resolve(__dirname, 'dist'),
    filename: 'main.js',
    clean: true,
    publicPath: '/',
  },
  mode: 'development',
  devServer: {
    static: path.join(__dirname, 'public'),
    historyApiFallback: true,
    port: 8082
  },
  resolve: {
    extensions: ['.js', '.jsx']
  },
  module: {
    rules: [
      {
        test: /\\.jsx?$/,
        exclude: /node_modules/,
        use: {
          loader: 'babel-loader'
        }
      }
    ]
  },
  plugins: [
    new HtmlWebpackPlugin({
      template: './public/index.html'
    })
  ]
};
EOF

# Create .babelrc for Babel configuration
cat << 'EOF' > .babelrc
{
  "presets": ["@babel/preset-env", "@babel/preset-react"]
}
EOF

# Update package.json start script (using jq if available, else instruct the user)
if command -v jq &> /dev/null; then
  tmp=$(mktemp)
  jq '.scripts.start = "webpack serve --mode development"' package.json > "$tmp" && mv "$tmp" package.json
else
  echo "Please update your package.json scripts section to include:"
  echo '"start": "webpack serve --mode development"'
fi

echo "Project setup complete. You can now run 'npm start' to launch the dashboard."
