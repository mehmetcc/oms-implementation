package org.mehmetcc.account.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mehmetcc.account.constants.CurrencyConstants;
import org.mehmetcc.account.event.BuyOrderCreatedEvent;
import org.mehmetcc.account.event.OrderEvent;
import org.mehmetcc.account.event.SellOrderCreatedEvent;
import org.mehmetcc.account.model.Asset;
import org.mehmetcc.account.model.Order;
import org.mehmetcc.account.repository.AssetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class AssetService {
    private final AssetRepository repository;

    public Optional<String> create(final Asset asset) {
        try {
            Objects.requireNonNull(asset);
            return Optional.of(repository.save(asset).getId());
        } catch (Exception e) {
            log.error("Exception occurred during asset creation: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public List<Asset> readAll() {
        return repository.findAll();
    }

    public void process(final OrderEvent event) {
        switch (event) {
            case BuyOrderCreatedEvent created -> buy(created.getOrder());
            case SellOrderCreatedEvent updated -> sell(updated.getOrder());
            default ->
                    throw new IllegalArgumentException("Unsupported event type: " + event.getClass().getSimpleName());
        }
    }

    /**
     * AssetService::buy and related methods
     **/
    @Transactional
    private void buy(final Order order) {
        if (checkBalance(order)) processPurchaseOrder(order); // if balance is enough
        else processInsufficientFunds(order); // if balance is not enough
    }

    private void processInsufficientFunds(final Order order) {
        log.error("Insufficient funds for order id: {}", order.getId());
        // TODO: fire an OrderCancelled event here
    }

    private void processPurchaseOrder(final Order order) {
        if (repository.existsByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName())) // if asset exists in db
            processPurchaseForExistingAsset(order);
        else // otherwise
            processPurchaseForNonExistingAsset(order);
    }

    private void processPurchaseForExistingAsset(final Order order) {
        var asset = repository.findByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName());
        asset.setUsableSize(asset.getUsableSize().add(order.getSize()));
        asset.setTotalSize(asset.getTotalSize().add(order.getSize()));

        var lira = repository.findByCustomerIdAndAssetName(order.getCustomerId(), CurrencyConstants.TRY);
        lira.setUsableSize(lira.getUsableSize().subtract(order.totalPrice()));
        lira.setTotalSize(lira.getTotalSize().subtract(order.totalPrice()));

        repository.saveAll(List.of(lira, asset));
        // TODO: fire an OrderMatched event here
    }

    @Transactional
    private void processPurchaseForNonExistingAsset(final Order order) {
        var asset = Asset.builder()
                .customerId(order.getCustomerId())
                .assetName(order.getAssetName())
                .totalSize(order.getSize())
                .usableSize(order.getSize())
                .build();

        var lira = repository.findByCustomerIdAndAssetName(order.getCustomerId(), CurrencyConstants.TRY);
        lira.setUsableSize(lira.getUsableSize().subtract(order.totalPrice()));
        lira.setTotalSize(lira.getTotalSize().subtract(order.totalPrice()));

        repository.saveAll(List.of(lira, asset));
        // TODO: fire an OrderMatched event here
    }

    private Boolean checkBalance(final Order order) {
        var currentBalance = repository.findByCustomerIdAndAssetName(order.getCustomerId(), CurrencyConstants.TRY);
        if (currentBalance != null) return currentBalance.getUsableSize().compareTo(order.totalPrice()) >= 0;
        else return false;
    }

    /**
     * AssetService::sell and related methods
     **/
    private void sell(final Order order) {
        if (checkUsableSize(order)) processSaleOrder(order);
        else processInsufficientSize(order);
    }

    private void processSaleOrder(final Order order) {
        var asset = repository.findByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName());
        asset.setUsableSize(asset.getUsableSize().subtract(order.getSize()));
        asset.setTotalSize(asset.getTotalSize().subtract(order.getSize()));

        var lira = repository.findByCustomerIdAndAssetName(order.getCustomerId(), CurrencyConstants.TRY);
        lira.setUsableSize(lira.getUsableSize().add(order.totalPrice()));
        lira.setTotalSize(lira.getTotalSize().add(order.totalPrice()));

        repository.saveAll(List.of(lira, asset));
        // TODO: fire an OrderMatched event here
    }

    private void processInsufficientSize(final Order order) {
        log.error("Insufficient number of holdings for order id: {}", order.getId());
        // TODO: fire an OrderCancelled event here

    }

    private Boolean checkUsableSize(final Order order) {
        var currentSize = repository.findByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName());
        if (currentSize != null) return currentSize.getUsableSize().compareTo(order.getSize()) >= 0;
        else return false;
    }
}
