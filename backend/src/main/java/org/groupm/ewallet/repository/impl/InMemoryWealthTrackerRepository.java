package org.groupm.ewallet.repository.impl;

import org.groupm.ewallet.repository.WealthTrackerRepository;
import org.groupm.ewallet.model.WealthTracker;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository mémoire pour WealthTracker.
 */
public class InMemoryWealthTrackerRepository implements WealthTrackerRepository {

    private final Map<String, WealthTracker> trackers = new HashMap<>();

    @Override
    public void save(WealthTracker tracker) {
        trackers.put(tracker.getUser().getUserID(), tracker);
    }

    @Override
    public WealthTracker findByUserId(String userID) {
        return trackers.get(userID);
    }

    @Override
    public void delete(String userID) {
        trackers.remove(userID);
    }
}
