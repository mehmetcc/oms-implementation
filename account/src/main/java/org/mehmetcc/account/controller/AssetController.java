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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/assets")
public class AssetController {
    private final AssetService service;

    @PostMapping
    public ResponseEntity<CreateAssetResponse> create(@Valid @RequestBody final CreateAssetRequest request) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        // filter customers
        if (!isAdmin && !request.getCustomerId().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var created = service.create(request.toAsset());
        return created
                .map(result -> ResponseEntity.ok(new CreateAssetResponse(result)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping
    public ResponseEntity<ListAssetResponse> all(@RequestParam(required = false) final String customerId,
                                                 @RequestParam(required = false) final String assetName) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        var effectiveCustomerId = customerId;

        // filter customers
        if (!isAdmin) {
            if (customerId != null && !customerId.equals(auth.getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            effectiveCustomerId = auth.getName();
        }

        var assets = service.readAll(effectiveCustomerId, assetName)
                .stream()
                .map(asset -> GetAssetResponse.builder()
                        .customerId(asset.getCustomerId())
                        .assetName(asset.getAssetName())
                        .totalSize(asset.getTotalSize())
                        .usableSize(asset.getUsableSize())
                        .build())
                .toList();
        return ResponseEntity.ok(new ListAssetResponse(assets));
    }
}
