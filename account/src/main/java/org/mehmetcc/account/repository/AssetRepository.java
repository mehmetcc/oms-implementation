package org.mehmetcc.account.repository;

import org.mehmetcc.account.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, String> {
    // Find all accounts for a specific customer
    List<Asset> findByCustomerId(String customerId);

    // Optionally, find an account for a specific customer and asset
    Asset findByCustomerIdAndAssetName(String customerId, String assetName);
}
