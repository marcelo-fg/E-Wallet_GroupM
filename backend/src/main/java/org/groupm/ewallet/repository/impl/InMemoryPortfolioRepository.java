package org.groupm.ewallet.repository.impl;

import org.groupm.ewallet.model.Portfolio;
import org.groupm.ewallet.repository.PortfolioRepository;

import java.util.*;
import java.util.stream.Collectors;

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
    public List<Portfolio> findAll() {
        return new ArrayList<>(portfolios.values());
    }

    @Override
    public List<Portfolio> findAllByUserId(String userID) {
        return portfolios.values().stream()
                .filter(p -> p.getUserID() != null && p.getUserID().equals(userID))
                .collect(Collectors.toList());
    }
}