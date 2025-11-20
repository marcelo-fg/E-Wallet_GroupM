package org.groupm.ewallet.mapper;

import org.groupm.ewallet.model.Portfolio;
import org.groupm.ewallet.dto.PortfolioDTO;
import org.groupm.ewallet.dto.AssetDTO;
import org.groupm.ewallet.model.Asset;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Conversion Portfolio ↔ PortfolioDTO.
 */
public final class PortfolioMapper {

    private PortfolioMapper() {}

    public static PortfolioDTO toDto(Portfolio portfolio) {
        if (portfolio == null) return null;
        PortfolioDTO dto = new PortfolioDTO();
        dto.setId(portfolio.getId());
        dto.setUserID(portfolio.getUserID());
        List<AssetDTO> assetsDto = portfolio.getAssets().stream()
                .map(AssetMapper::toDto)
                .collect(Collectors.toList());
        dto.setAssets(assetsDto);
        return dto;
    }

    public static Portfolio toEntity(PortfolioDTO dto) {
        if (dto == null) return null;
        Portfolio portfolio = new Portfolio(dto.getId(), dto.getUserID());
        List<Asset> assets = dto.getAssets().stream()
                .map(AssetMapper::toEntity)
                .collect(Collectors.toList());
        portfolio.setAssets(assets);
        return portfolio;
    }
}
