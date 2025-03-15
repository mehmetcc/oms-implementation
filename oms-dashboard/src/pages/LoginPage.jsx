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
