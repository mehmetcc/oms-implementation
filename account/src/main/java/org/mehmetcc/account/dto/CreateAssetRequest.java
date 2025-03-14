package org.mehmetcc.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.mehmetcc.account.model.Asset;

import java.math.BigDecimal;

@Data
public class CreateAssetRequest {
    @NotBlank
    private String customerId;

    @NotBlank
    private String assetName;

    @NotNull
    private BigDecimal totalSize;

    @NotNull
    private BigDecimal usableSize;

    public Asset toAsset() {
        return Asset.builder()
                .customerId(customerId)
                .assetName(assetName)
                .totalSize(totalSize)
                .usableSize(usableSize)
                .build();
    }
}
