package org.groupm.ewallet.service.business;

import org.groupm.ewallet.model.WealthTracker;

public interface TotalWealthService {
    WealthTracker getCurrentUserWealth();

    WealthTracker getWealthForUser(String userId);
}
