package org.mehmetcc.account.controller;

import org.mehmetcc.account.model.Asset;
import org.mehmetcc.account.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assets")
public class AssetController {
    private final AssetService service;

    @Autowired
    public AssetController(final AssetService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Asset>> getAssets(@RequestParam("customerId") final String customerId) {
        var assets = service.getAssetsByCustomer(customerId);
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Asset> getAssetById(@PathVariable("id") final String id) {
        return service
                .getAssetById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
