package org.groupm.ewallet.service.business;

import org.groupm.ewallet.model.Transaction;
import org.groupm.ewallet.repository.TransactionRepository;

import java.util.List;

/**
 * Service métier dédié à la gestion des transactions financières.
 * Permet de créer, récupérer, modifier, supprimer et filtrer les transactions associées aux comptes.
 */
public class TransactionManager {

    private final TransactionRepository transactionRepository;

    /**
     * Constructeur avec injection de repository.
     * @param transactionRepository repository de persistance
     */
    public TransactionManager(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Crée et persiste une nouvelle transaction.
     * @param transaction transaction à enregistrer
     * @return transaction créée
     */
    public Transaction addTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
        return transaction;
    }

    /**
     * Récupère toutes les transactions enregistrées.
     * @return liste complète des transactions
     */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /**
     * Recherche une transaction par son identifiant (String, pour cohérence).
     * @param transactionID identifiant unique
     * @return transaction ou null
     */
    public Transaction getTransactionById(String transactionID) {
        return transactionRepository.findById(transactionID);
    }

    /**
     * Supprime une transaction par son identifiant.
     * @param transactionID identifiant unique
     * @return true si la suppression est réussie
     */
    public boolean deleteTransaction(String transactionID) {
        Transaction tx = transactionRepository.findById(transactionID);
        if (tx != null) {
            transactionRepository.delete(transactionID);
            return true;
        }
        return false;
    }

    /**
     * Met à jour une transaction existante.
     * Seuls les champs non nuls du nouvel objet sont mis à jour.
     * @param transactionID identifiant transaction à modifier
     * @param newTransaction nouveau modèle transaction
     * @return true si mise à jour réussie
     */
    public boolean updateTransaction(String transactionID, Transaction newTransaction) {
        Transaction tx = transactionRepository.findById(transactionID);
        if (tx != null) {
            if (newTransaction.getType() != null) tx.setType(newTransaction.getType());
            if (newTransaction.getAmount() != 0) tx.setAmount(newTransaction.getAmount());
            if (newTransaction.getDescription() != null) tx.setDescription(newTransaction.getDescription());
            transactionRepository.save(tx);
            return true;
        }
        return false;
    }


    /**
     * Récupère toutes les transactions associées à un compte donné.
     * @param accountID identifiant du compte
     * @return liste des transactions du compte
     */
    public List<Transaction> getTransactionsByAccountId(String accountID) {
        return transactionRepository.findByAccountId(accountID);
    }

    /**
     * Récupère toutes les transactions d'un type donné (ex : "deposit").
     * @param type type de transaction
     * @return liste des transactions filtrées
     */
    public List<Transaction> getTransactionsByType(String type) {
        return transactionRepository.findByType(type);
    }
}
