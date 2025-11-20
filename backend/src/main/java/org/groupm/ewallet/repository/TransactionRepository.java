package org.groupm.ewallet.repository;

import org.groupm.ewallet.model.Transaction;
import java.util.List;

/**
 * Interface de gestion de la persistance des transactions financières.
 */
public interface TransactionRepository {
    void save(Transaction transaction);
    Transaction findById(String transactionID);
    void delete(String transactionID);
    List<Transaction> findAll();

    /**
     * Recherche toutes les transactions associées à un compte spécifique.
     * @param accountID identifiant du compte
     * @return liste des transactions du compte
     */
    List<Transaction> findByAccountId(String accountID);

    /**
     * Recherche toutes les transactions d’un type donné.
     * @param type type de transaction (ex : "deposit")
     * @return liste des transactions du type
     */
    List<Transaction> findByType(String type);
}
