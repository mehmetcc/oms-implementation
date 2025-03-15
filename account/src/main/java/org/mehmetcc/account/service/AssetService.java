package org.mehmetcc.account.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mehmetcc.account.constants.CurrencyConstants;
import org.mehmetcc.account.event.BuyOrderCreatedReceivedEvent;
import org.mehmetcc.account.event.OrderProcessedEvent;
import org.mehmetcc.account.event.OrderReceivedEvent;
import org.mehmetcc.account.event.SellOrderCreatedReceivedEvent;
import org.mehmetcc.account.model.Asset;
import org.mehmetcc.account.model.Order;
import org.mehmetcc.account.repository.AssetRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
public class AssetService {
    private final AssetRepository repository;

    private final OrderProcessedEventProducer producer;

    public Asset create(final Asset asset) {
        if (repository.existsByCustomerIdAndAssetName(asset.getCustomerId(), asset.getAssetName()))
            return updateExistingAsset(asset);

        return createNewAsset(asset);
    }

    private Asset createNewAsset(final Asset asset) {
        Objects.requireNonNull(asset, "Asset cannot be null");

        if (asset.getId() != null)
            throw new IllegalArgumentException("Asset already has an ID. Use updateAsset to update existing records.");

        return repository.save(asset);
    }

    private Asset updateExistingAsset(final Asset asset) {
        Objects.requireNonNull(asset, "Asset cannot be null");

        var existingAsset = repository.findByCustomerIdAndAssetName(asset.getCustomerId(), asset.getAssetName());
        if (existingAsset == null) {
            throw new IllegalArgumentException("Asset not found for update, customerId: "
                    + asset.getCustomerId() + ", assetName: " + asset.getAssetName());
        }

        existingAsset.setTotalSize(existingAsset.getTotalSize().add(asset.getTotalSize()));
        existingAsset.setUsableSize(existingAsset.getUsableSize().add(asset.getUsableSize()));

        return repository.save(existingAsset);
    }

    public List<Asset> readAll(final String customerId, final String assetName) {
        return repository.findAll().stream()
                .filter(asset -> customerId == null || asset.getCustomerId().equals(customerId))
                .filter(asset -> assetName == null || asset.getAssetName().equalsIgnoreCase(assetName))
                .toList();
    }

    public OrderProcessedEvent process(final OrderReceivedEvent event) {
        switch (event) {
            case BuyOrderCreatedReceivedEvent created -> {
                return buy(created.getOrder());
            }
            case SellOrderCreatedReceivedEvent updated -> {
                return sell(updated.getOrder());
            }
            default ->
                    throw new IllegalArgumentException("Unsupported event type: " + event.getClass().getSimpleName());
        }
    }

    /**
     * AssetService::buy and related methods
     **/
    @Transactional
    private OrderProcessedEvent buy(final Order order) {
        if (checkBalance(order)) return processPurchaseOrder(order); // if balance is enough
        else return processInsufficientFunds(order); // if balance is not enough
    }

    private OrderProcessedEvent processInsufficientFunds(final Order order) {
        log.error("Insufficient funds for order id: {}", order.getId());
        return producer.sendOrderProcessedEvent(OrderProcessedEvent.cancelled(order.getId()));
    }

    private OrderProcessedEvent processPurchaseOrder(final Order order) {
        if (repository.existsByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName())) // if asset exists in db
            return processPurchaseForExistingAsset(order);
        else // otherwise
            return processPurchaseForNonExistingAsset(order);
    }

    private OrderProcessedEvent processPurchaseForExistingAsset(final Order order) {
        var asset = repository.findByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName());
        log.info("Asset intercepted: {}", asset);
        asset.setUsableSize(asset.getUsableSize().add(order.getSize()));
        asset.setTotalSize(asset.getTotalSize().add(order.getSize()));

        var lira = repository.findByCustomerIdAndAssetName(order.getCustomerId(), CurrencyConstants.TRY);
        lira.setUsableSize(lira.getUsableSize().subtract(order.totalPrice()));
        lira.setTotalSize(lira.getTotalSize().subtract(order.totalPrice()));

        repository.save(lira);
        repository.save(asset);
        return producer.sendOrderProcessedEvent(OrderProcessedEvent.matched(order.getId()));
    }

    @Transactional
    private OrderProcessedEvent processPurchaseForNonExistingAsset(final Order order) {
        var asset = Asset.builder()
                .customerId(order.getCustomerId())
                .assetName(order.getAssetName())
                .totalSize(order.getSize())
                .usableSize(order.getSize())
                .build();
        log.info("Asset created: {}", asset);

        var lira = repository.findByCustomerIdAndAssetName(order.getCustomerId(), CurrencyConstants.TRY);
        lira.setUsableSize(lira.getUsableSize().subtract(order.totalPrice()));
        lira.setTotalSize(lira.getTotalSize().subtract(order.totalPrice()));

        repository.saveAll(List.of(lira, asset));
        return producer.sendOrderProcessedEvent(OrderProcessedEvent.matched(order.getId()));
    }

    private Boolean checkBalance(final Order order) {
        var currentBalance = repository.findByCustomerIdAndAssetName(order.getCustomerId(), CurrencyConstants.TRY);
        if (currentBalance != null) return currentBalance.getUsableSize().compareTo(order.totalPrice()) >= 0;
        else return false;
    }

    /**
     * AssetService::sell and related methods
     **/
    private OrderProcessedEvent sell(final Order order) {
        if (checkUsableSize(order)) return processSaleOrder(order);
        else return processInsufficientSize(order);
    }

    private OrderProcessedEvent processSaleOrder(final Order order) {
        var asset = repository.findByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName());
        // i forgot about this case where lira can't be found
        // a null check is already present for the variable asset but this totally caught me off guard
        // thankfully, unit testing exists
        var lira = repository.findByCustomerIdAndAssetName(order.getCustomerId(), CurrencyConstants.TRY);
        if (lira == null) {
            log.warn("Lira asset not found for customer {}, creating new balance.", order.getCustomerId());
            lira = Asset.builder()
                    .customerId(order.getCustomerId())
                    .assetName(CurrencyConstants.TRY)
                    .totalSize(BigDecimal.ZERO)
                    .usableSize(BigDecimal.ZERO)
                    .build();
            repository.save(lira);
        }

        // Process the sale
        asset.setUsableSize(asset.getUsableSize().subtract(order.getSize()));
        asset.setTotalSize(asset.getTotalSize().subtract(order.getSize()));

        lira.setUsableSize(lira.getUsableSize().add(order.totalPrice()));
        lira.setTotalSize(lira.getTotalSize().add(order.totalPrice()));

        repository.saveAll(List.of(asset, lira));
        return producer.sendOrderProcessedEvent(OrderProcessedEvent.matched(order.getId()));
    }

    private OrderProcessedEvent processInsufficientSize(final Order order) {
        log.error("Insufficient number of holdings for order id: {}", order.getId());
        return producer.sendOrderProcessedEvent(OrderProcessedEvent.cancelled(order.getId()));
    }

    private Boolean checkUsableSize(final Order order) {
        var currentSize = repository.findByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName());
        if (currentSize != null) return currentSize.getUsableSize().compareTo(order.getSize()) >= 0;
        else return false;
    }
}
