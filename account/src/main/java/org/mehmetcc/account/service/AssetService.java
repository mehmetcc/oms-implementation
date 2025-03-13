package org.mehmetcc.account.service;

import org.mehmetcc.account.model.Asset;
import org.mehmetcc.account.repository.AssetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * This is not going to be customer-facing.
 * So, we don't care about error handling that much (hence the lack of try-catch mechanisms
 **/
@Service
public class AssetService {
    private final AssetRepository repository;

    public AssetService(final AssetRepository repository) {
        this.repository = repository;
    }

    public List<Asset> getAssetsByCustomer(String customerId) {
        return repository.findByCustomerId(customerId);
    }

    public Optional<Asset> getAssetById(String assetId) {
        return repository.findById(assetId);
    }
}
