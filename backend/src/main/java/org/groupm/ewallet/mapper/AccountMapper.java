package org.groupm.ewallet.mapper;

import org.groupm.ewallet.model.Account;
import org.groupm.ewallet.dto.AccountDTO;
import org.groupm.ewallet.model.Transaction;
import org.groupm.ewallet.dto.TransactionDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utilitaire de conversion entre la classe métier Account et son DTO.
 */
public final class AccountMapper {

    private AccountMapper() {}

    public static AccountDTO toDto(Account account) {
        if (account == null) return null;
        AccountDTO dto = new AccountDTO();
        dto.setAccountID(account.getAccountID());
        dto.setType(account.getType());
        dto.setBalance(account.getBalance());
        // Transactions imbriquées
        List<TransactionDTO> txDto = account.getTransactions().stream()
                .map(TransactionMapper::toDto)
                .collect(Collectors.toList());
        dto.setTransactions(txDto);
        return dto;
    }

    public static Account toEntity(AccountDTO dto) {
        if (dto == null) return null;
        Account account = new Account();
        account.setAccountID(dto.getAccountID());
        account.setType(dto.getType());
        account.setBalance(dto.getBalance());
        List<Transaction> txList = dto.getTransactions().stream()
                .map(TransactionMapper::toEntity)
                .collect(Collectors.toList());
        account.getTransactions().addAll(txList);
        return account;
    }
}
