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
    <div>
      <h2>Create Asset</h2>
      <form onSubmit={handleSubmit}>
        {role === 'admin' && (
          <label>
            CustomerId:
            <input
              value={form.customerId}
              onChange={e => setForm({ ...form, customerId: e.target.value })}
            />
          </label>
        )}
        <br/>
        <label>
          Asset Name:
          <input
            value={form.assetName}
            onChange={e => setForm({ ...form, assetName: e.target.value })}
          />
        </label>
        <br/>
        <label>
          Total Size:
          <input
            type="number"
            value={form.totalSize}
            onChange={e => setForm({ ...form, totalSize: Number(e.target.value) })}
          />
        </label>
        <br/>
        <label>
          Usable Size:
          <input
            type="number"
            value={form.usableSize}
            onChange={e => setForm({ ...form, usableSize: Number(e.target.value) })}
          />
        </label>
        <br/>
        <button type="submit">Create Asset</button>
      </form>
    </div>
  );
}

export default AssetCreatePage;
