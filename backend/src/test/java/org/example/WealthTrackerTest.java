package org.example;

import org.example.model.Asset;
import org.example.model.Portfolio;
import org.example.model.User;
import org.example.model.WealthTracker;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WealthTrackerTest {

    @Test
    void emptyPortfolioWealthIsZero() {
        User user = new User("2", "empty@example.com", "1234", "Bob", "Empty");
        WealthTracker tracker = new WealthTracker(user);
        tracker.updateWealth();
        assertEquals(0.0, tracker.getTotalWealth(), 1e-6);
    }
}