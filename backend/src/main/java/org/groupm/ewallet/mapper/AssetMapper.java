package org.groupm.ewallet.mapper;

import org.groupm.ewallet.model.Asset;
import org.groupm.ewallet.dto.AssetDTO;

/**
 * Utilitaire de conversion Asset ↔ AssetDTO.
 */
public final class AssetMapper {

    private AssetMapper() {}

    public static AssetDTO toDto(Asset asset) {
        if (asset == null) return null;
        AssetDTO dto = new AssetDTO();
        dto.setSymbol(asset.getSymbol());
        dto.setType(asset.getType());
        dto.setAssetName(asset.getAssetName());
        dto.setUnitValue(asset.getUnitValue());
        dto.setQuantity(asset.getQuantity());
        return dto;
    }

    public static Asset toEntity(AssetDTO dto) {
        if (dto == null) return null;
        Asset asset = new Asset(dto.getSymbol(), dto.getType(), dto.getAssetName(), dto.getUnitValue());
        asset.setQuantity(dto.getQuantity());
        return asset;
    }
}
