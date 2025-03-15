package org.mehmetcc.account.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mehmetcc.account.constants.CurrencyConstants;
import org.mehmetcc.account.event.BuyOrderCreatedReceivedEvent;
import org.mehmetcc.account.event.OrderProcessedEvent;
import org.mehmetcc.account.event.SellOrderCreatedReceivedEvent;
import org.mehmetcc.account.model.Asset;
import org.mehmetcc.account.model.Order;
import org.mehmetcc.account.model.OrderStatus;
import org.mehmetcc.account.repository.AssetRepository;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AssetServiceTest {
    @Mock
    private AssetRepository assetRepository;

    @Mock
    private OrderProcessedEventProducer producer;

    private AssetService assetService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        assetService = new AssetService(assetRepository, producer);
    }

    @Test
    void testCreateAssetSuccess_NewAsset() {
        // Arrange: new asset should have null ID.
        Asset asset = createSampleAsset(null, "AAPL", BigDecimal.valueOf(10), BigDecimal.valueOf(10));
        Asset savedAsset = createSampleAsset("asset1", "AAPL", BigDecimal.valueOf(10), BigDecimal.valueOf(10));
        when(assetRepository.existsByCustomerIdAndAssetName("customer1", "AAPL")).thenReturn(false);
        when(assetRepository.save(asset)).thenReturn(savedAsset);

        // Act
        Asset result = assetService.create(asset);

        // Assert
        assertThat(result.getId()).isEqualTo("asset1");
        verify(assetRepository).save(asset);
    }

    @Test
    void testCreateAssetFailure_NewAsset() {
        // Arrange: simulate repository.save throwing an exception.
        Asset asset = createSampleAsset(null, "AAPL", BigDecimal.valueOf(10), BigDecimal.valueOf(10));
        when(assetRepository.existsByCustomerIdAndAssetName("customer1", "AAPL")).thenReturn(false);
        when(assetRepository.save(asset)).thenThrow(new RuntimeException("Database error"));

        // Act + Assert
        assertThatThrownBy(() -> assetService.create(asset))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database error");
        verify(assetRepository).save(asset);
    }

    @Test
    void testUpdateExistingAsset() {
        // Arrange: simulate update scenario
        // Incoming asset has null ID (or ignored), but exists in DB.
        Asset assetToUpdate = createSampleAsset(null, "AAPL", BigDecimal.valueOf(5), BigDecimal.valueOf(5));
        Asset existingAsset = createSampleAsset("asset1", "AAPL", BigDecimal.valueOf(10), BigDecimal.valueOf(10));
        when(assetRepository.existsByCustomerIdAndAssetName("customer1", "AAPL")).thenReturn(true);
        when(assetRepository.findByCustomerIdAndAssetName("customer1", "AAPL")).thenReturn(existingAsset);
        when(assetRepository.save(existingAsset)).thenReturn(existingAsset);

        // Act
        Asset result = assetService.create(assetToUpdate);

        // Assert: updated sizes: 10+5=15
        assertThat(result.getTotalSize()).isEqualByComparingTo(BigDecimal.valueOf(15));
        assertThat(result.getUsableSize()).isEqualByComparingTo(BigDecimal.valueOf(15));
        verify(assetRepository).findByCustomerIdAndAssetName("customer1", "AAPL");
        verify(assetRepository).save(existingAsset);
    }

    @Test
    void testReadAllAssets() {
        // Arrange
        Asset asset1 = createSampleAsset("asset1", "AAPL", BigDecimal.valueOf(10), BigDecimal.valueOf(10));
        Asset asset2 = createSampleAsset("asset2", "GOOG", BigDecimal.valueOf(20), BigDecimal.valueOf(20));
        when(assetRepository.findAll()).thenReturn(List.of(asset1, asset2));

        // Act
        List<Asset> assets = assetService.readAll(null, null);

        // Assert
        assertThat(assets).hasSize(2).containsExactly(asset1, asset2);
        verify(assetRepository).findAll();
    }

    @Test
    void testProcessBuyOrderWithSufficientBalance() {
        // Arrange
        Order order = createSampleOrder("order1", "AAPL", BigDecimal.valueOf(5), BigDecimal.valueOf(100));
        Asset balance = createSampleAsset("balance1", CurrencyConstants.TRY, BigDecimal.valueOf(1000), BigDecimal.valueOf(1000));
        when(assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), CurrencyConstants.TRY))
                .thenReturn(balance);
        when(producer.sendOrderProcessedEvent(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderProcessedEvent event = assetService.process(new BuyOrderCreatedReceivedEvent(order));

        // Assert
        assertThat(event.getStatus()).isEqualTo(OrderStatus.MATCHED);
        verify(assetRepository, atLeastOnce()).saveAll(any());
        verify(producer).sendOrderProcessedEvent(any(OrderProcessedEvent.class));
    }

    @Test
    void testProcessBuyOrderWithInsufficientBalance() {
        // Arrange
        Order order = createSampleOrder("order1", "AAPL", BigDecimal.valueOf(5), BigDecimal.valueOf(100));
        Asset balance = createSampleAsset("balance1", CurrencyConstants.TRY, BigDecimal.valueOf(50), BigDecimal.valueOf(50));
        when(assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), CurrencyConstants.TRY))
                .thenReturn(balance);
        when(producer.sendOrderProcessedEvent(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderProcessedEvent event = assetService.process(new BuyOrderCreatedReceivedEvent(order));

        // Assert
        assertThat(event.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(producer).sendOrderProcessedEvent(any(OrderProcessedEvent.class));
    }

    @Test
    void testProcessSellOrderWithSufficientHoldings() {
        // Arrange
        Order order = createSampleOrder("order1", "AAPL", BigDecimal.valueOf(5), BigDecimal.valueOf(100));
        Asset asset = createSampleAsset("asset1", "AAPL", BigDecimal.valueOf(10), BigDecimal.valueOf(10));
        when(assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName()))
                .thenReturn(asset);
        when(producer.sendOrderProcessedEvent(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderProcessedEvent event = assetService.process(new SellOrderCreatedReceivedEvent(order));

        // Assert
        assertThat(event.getStatus()).isEqualTo(OrderStatus.MATCHED);
        verify(assetRepository, atLeastOnce()).saveAll(any());
        verify(producer).sendOrderProcessedEvent(any(OrderProcessedEvent.class));
    }

    @Test
    void testProcessSellOrderWithInsufficientHoldings() {
        // Arrange
        Order order = createSampleOrder("order1", "AAPL", BigDecimal.valueOf(10), BigDecimal.valueOf(100));
        Asset asset = createSampleAsset("asset1", "AAPL", BigDecimal.valueOf(5), BigDecimal.valueOf(5));
        when(assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName()))
                .thenReturn(asset);
        when(producer.sendOrderProcessedEvent(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderProcessedEvent event = assetService.process(new SellOrderCreatedReceivedEvent(order));

        // Assert
        assertThat(event.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(producer).sendOrderProcessedEvent(any(OrderProcessedEvent.class));
    }

    @Test
    void testProcessSellOrderEnsuresLiraAssetExists() {
        // Arrange
        Order order = createSampleOrder("order1", "AAPL", BigDecimal.valueOf(5), BigDecimal.valueOf(100));
        Asset asset = createSampleAsset("asset1", "AAPL", BigDecimal.valueOf(10), BigDecimal.valueOf(10));
        when(assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName()))
                .thenReturn(asset);
        when(assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), CurrencyConstants.TRY))
                .thenReturn(null);
        when(producer.sendOrderProcessedEvent(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderProcessedEvent event = assetService.process(new SellOrderCreatedReceivedEvent(order));

        // Assert
        assertThat(event.getStatus()).isEqualTo(OrderStatus.MATCHED);
        verify(assetRepository).save(any(Asset.class));  // Ensures Lira asset is created
        verify(assetRepository, atLeastOnce()).saveAll(any());
        verify(producer).sendOrderProcessedEvent(any(OrderProcessedEvent.class));
    }

    private Asset createSampleAsset(String id, String assetName, BigDecimal totalSize, BigDecimal usableSize) {
        return Asset.builder()
                .id(id)
                .customerId("customer1")
                .assetName(assetName)
                .totalSize(totalSize)
                .usableSize(usableSize)
                .build();
    }

    private Order createSampleOrder(String id, String assetName, BigDecimal size, BigDecimal price) {
        return Order.builder()
                .id(id)
                .customerId("customer1")
                .assetName(assetName)
                .size(size)
                .price(price)
                .status(OrderStatus.PENDING)
                .build();
    }
}
