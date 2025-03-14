package org.mehmetcc.account.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class GetAssetResponse {
    private String customerId;
    private String assetName;
    private BigDecimal totalSize;
    private BigDecimal usableSize;
}
