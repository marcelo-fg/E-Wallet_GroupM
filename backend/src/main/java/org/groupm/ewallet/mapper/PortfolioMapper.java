package org.groupm.ewallet.mapper;

import org.groupm.ewallet.model.Portfolio;
import org.groupm.ewallet.dto.PortfolioDTO;
import org.groupm.ewallet.dto.AssetDTO;
import org.groupm.ewallet.model.Asset;

import java.util.List;
import java.util.stream.Collectors;

public final class PortfolioMapper {

    private PortfolioMapper() {}

    // ====================== ENTITY → DTO ======================
    public static PortfolioDTO toDto(Portfolio portfolio) {
        if (portfolio == null) return null;

        PortfolioDTO dto = new PortfolioDTO();
        dto.setId(portfolio.getId());
        dto.setUserID(portfolio.getUserID());

        List<AssetDTO> assetsDto =
                (portfolio.getAssets() != null)
                        ? portfolio.getAssets().stream()
                        .map(AssetMapper::toDto)
                        .collect(Collectors.toList())
                        : java.util.Collections.emptyList();

        dto.setAssets(assetsDto);

        dto.setTotalValue(portfolio.getTotalValue());

        return dto;
    }

    // ====================== DTO → ENTITY ======================
    public static Portfolio toEntity(PortfolioDTO dto) {
        if (dto == null) return null;

        Portfolio portfolio = new Portfolio();
        portfolio.setId(dto.getId());    // important si DTO restaure un portefeuille existant
        portfolio.setUserID(dto.getUserID());

        List<Asset> assets =
                (dto.getAssets() != null)
                        ? dto.getAssets().stream()
                        .map(AssetMapper::toEntity)
                        .collect(Collectors.toList())
                        : java.util.Collections.emptyList();

        portfolio.setAssets(assets);

        return portfolio;
    }
}