package org.mehmetcc.account.repository;

import org.mehmetcc.account.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, String> {
    List<Asset> findByCustomerId(String customerId);

    Asset findByCustomerIdAndAssetName(String customerId, String assetName);
}
