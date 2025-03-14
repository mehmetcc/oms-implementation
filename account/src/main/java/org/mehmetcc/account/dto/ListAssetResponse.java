package org.mehmetcc.account.dto;

import lombok.Data;

import java.util.List;

@Data
public class ListAssetResponse {
    private final List<GetAssetResponse> assets;
}
