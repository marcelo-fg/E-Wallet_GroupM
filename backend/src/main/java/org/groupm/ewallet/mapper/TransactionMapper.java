package org.groupm.ewallet.mapper;

import org.groupm.ewallet.model.Transaction;
import org.groupm.ewallet.dto.TransactionDTO;

import java.time.LocalDateTime;

/**
 * Conversion Transaction ↔ TransactionDTO.
 */
public final class TransactionMapper {

    private TransactionMapper() {}

    public static TransactionDTO toDto(Transaction tx) {
        if (tx == null) return null;
        TransactionDTO dto = new TransactionDTO();
        dto.setTransactionID(tx.getTransactionID());
        dto.setType(tx.getType());
        dto.setAmount(tx.getAmount());
        dto.setTimestamp(tx.getTimestamp());
        dto.setDescription(tx.getDescription());
        dto.setAccountID(tx.getAccountID());
        return dto;
    }

    public static Transaction toEntity(TransactionDTO dto) {
        if (dto == null) return null;
        Transaction tx = new Transaction(
                dto.getTransactionID(),
                dto.getType(),
                dto.getAmount(),
                dto.getDescription(),
                dto.getAccountID()
        );
        // Si la date doit être forcée (ex pour la réplication), ajoute une méthode dédiée
        return tx;
    }
}
