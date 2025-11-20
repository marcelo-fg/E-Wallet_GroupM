package org.groupm.ewallet.repository.impl;

import org.groupm.ewallet.repository.PortfolioRepository;
import org.groupm.ewallet.model.Portfolio;

import java.util.*;

/**
 * Repository mémoire pour Portfolio.
 */
public class InMemoryPortfolioRepository implements PortfolioRepository {

    private final Map<Integer, Portfolio> portfolios = new HashMap<>();

    @Override
    public void save(Portfolio portfolio) {
        portfolios.put(portfolio.getId(), portfolio);
    }

    @Override
    public Portfolio findById(int id) {
        return portfolios.get(id);
    }

    @Override
    public void delete(int id) {
        portfolios.remove(id);
    }

    @Override
    public Portfolio findByUserId(String userID) {
        return portfolios.values().stream()
                .filter(p -> p.getUserID() != null && p.getUserID().equals(userID))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Portfolio> findAll() {
        return new ArrayList<>(portfolios.values());
    }
}
