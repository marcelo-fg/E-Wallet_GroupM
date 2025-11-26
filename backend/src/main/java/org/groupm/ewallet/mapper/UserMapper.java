package org.groupm.ewallet.mapper;

import org.groupm.ewallet.model.User;
import org.groupm.ewallet.dto.UserDTO;
import org.groupm.ewallet.model.Account;
import org.groupm.ewallet.dto.AccountDTO;
import org.groupm.ewallet.dto.PortfolioDTO;
import org.groupm.ewallet.model.Portfolio;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Conversion User ↔ UserDTO (version multi-portefeuilles).
 */
public final class UserMapper {

    private UserMapper() {}

    // ============================================================
    //                      ENTITY -> DTO
    // ============================================================

    public static UserDTO toDto(User user) {
        if (user == null) return null;

        UserDTO dto = new UserDTO();
        dto.setUserID(user.getUserID());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());

        // Convertir les comptes
        List<AccountDTO> accountsDto = user.getAccounts().stream()
                .map(AccountMapper::toDto)
                .collect(Collectors.toList());
        dto.setAccounts(accountsDto);

        // Convertir la liste de portefeuilles
        List<PortfolioDTO> portfoliosDto = user.getPortfolios().stream()
                .map(PortfolioMapper::toDto)
                .collect(Collectors.toList());
        dto.setPortfolios(portfoliosDto);

        return dto;
    }

    // ============================================================
    //                      DTO -> ENTITY
    // ============================================================

    public static User toEntity(UserDTO dto) {
        if (dto == null) return null;

        User user = new User();
        user.setUserID(dto.getUserID());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());

        // Convertir comptes
        List<Account> accounts = dto.getAccounts().stream()
                .map(AccountMapper::toEntity)
                .collect(Collectors.toList());
        user.setAccounts(accounts);

        // Convertir portefeuilles
        List<Portfolio> portfolios = dto.getPortfolios().stream()
                .map(PortfolioMapper::toEntity)
                .collect(Collectors.toList());
        user.setPortfolios(portfolios);

        return user;
    }
}