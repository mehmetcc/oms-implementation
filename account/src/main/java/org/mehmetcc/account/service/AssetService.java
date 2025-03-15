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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class AssetService {
    private final AssetRepository repository;

    private final OrderProcessedEventProducer producer;

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

    public void process(final OrderReceivedEvent event) {
        switch (event) {
            case BuyOrderCreatedReceivedEvent created -> buy(created.getOrder());
            case SellOrderCreatedReceivedEvent updated -> sell(updated.getOrder());
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
        asset.setUsableSize(asset.getUsableSize().add(order.getSize()));
        asset.setTotalSize(asset.getTotalSize().add(order.getSize()));

        var lira = repository.findByCustomerIdAndAssetName(order.getCustomerId(), CurrencyConstants.TRY);
        lira.setUsableSize(lira.getUsableSize().subtract(order.totalPrice()));
        lira.setTotalSize(lira.getTotalSize().subtract(order.totalPrice()));

        repository.saveAll(List.of(lira, asset));
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
        asset.setUsableSize(asset.getUsableSize().subtract(order.getSize()));
        asset.setTotalSize(asset.getTotalSize().subtract(order.getSize()));

        var lira = repository.findByCustomerIdAndAssetName(order.getCustomerId(), CurrencyConstants.TRY);
        lira.setUsableSize(lira.getUsableSize().add(order.totalPrice()));
        lira.setTotalSize(lira.getTotalSize().add(order.totalPrice()));

        repository.saveAll(List.of(lira, asset));
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
