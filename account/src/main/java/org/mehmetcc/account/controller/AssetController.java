package org.mehmetcc.account.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mehmetcc.account.dto.CreateAssetRequest;
import org.mehmetcc.account.dto.CreateAssetResponse;
import org.mehmetcc.account.dto.GetAssetResponse;
import org.mehmetcc.account.dto.ListAssetResponse;
import org.mehmetcc.account.service.AssetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/assets")
public class AssetController {
    private final AssetService service;

    @PostMapping
    public ResponseEntity<CreateAssetResponse> create(@Valid @RequestBody final CreateAssetRequest request) {
        var created = service.create(request.toAsset());
        return created
                .map(result -> ResponseEntity.ok(new CreateAssetResponse(result)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping
    public ResponseEntity<ListAssetResponse> all() {
        return ResponseEntity.ok(new ListAssetResponse(service
                .readAll()
                .stream()
                .map(current -> GetAssetResponse.builder()
                        .customerId(current.getCustomerId())
                        .assetName(current.getAssetName())
                        .totalSize(current.getTotalSize())
                        .usableSize(current.getUsableSize())
                        .build()).toList()));
    }
}
