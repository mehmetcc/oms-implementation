import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

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
    <div>
      <h2>Register</h2>
      <form onSubmit={handleSubmit}>
        <label>
          Username:
          <input
            value={form.username}
            onChange={e => setForm({ ...form, username: e.target.value })}
          />
        </label>
        <br/>
        <label>
          Password:
          <input
            type="password"
            value={form.password}
            onChange={e => setForm({ ...form, password: e.target.value })}
          />
        </label>
        <br/>
        <label>
          Role:
          <select
            value={form.role}
            onChange={e => setForm({ ...form, role: e.target.value })}
          >
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
