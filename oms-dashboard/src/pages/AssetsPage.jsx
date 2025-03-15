import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../App';

const ASSET_URL = 'http://localhost:667/api/v1/assets';

function AssetsPage() {
  const { auth } = useContext(AuthContext);
  const [assets, setAssets] = useState([]);
  const role = JSON.parse(atob(auth.token.split('.')[1])).role;
  const [filterCustomerId, setFilterCustomerId] = useState('');

  const fetchAssets = async () => {
    const url = filterCustomerId ? `${ASSET_URL}?customerId=${filterCustomerId}` : ASSET_URL;
    const res = await fetch(url, {
      headers: {
        'Authorization': `Bearer ${auth.token}`
      }
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
            <input
              value={filterCustomerId}
              onChange={e => setFilterCustomerId(e.target.value)}
            />
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
