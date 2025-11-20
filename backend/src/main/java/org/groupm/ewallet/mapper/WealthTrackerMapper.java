package org.groupm.ewallet.mapper;

import org.groupm.ewallet.model.WealthTracker;
import org.groupm.ewallet.dto.WealthTrackerDTO;
import org.groupm.ewallet.model.User;


/**
 * Conversion WealthTracker ↔ WealthTrackerDTO.
 */
public final class WealthTrackerMapper {

    private WealthTrackerMapper() {}

    public static WealthTrackerDTO toDto(WealthTracker tracker) {
        if (tracker == null) return null;
        WealthTrackerDTO dto = new WealthTrackerDTO();
        dto.setUserID(tracker.getUser().getUserID());
        dto.setTotalWealthUsd(tracker.getTotalWealth());
        dto.setTotalWealthChf(tracker.getTotalWealthChf());
        dto.setGrowthRate(tracker.getGrowthRate());
        return dto;
    }

    // Pour l'entité, DTO -> WealthTracker nécessite un User (à adapter selon ton métier)
    public static WealthTracker toEntity(WealthTrackerDTO dto, User user) {
        if (dto == null || user == null) return null;
        WealthTracker tracker = new WealthTracker(user);
        // Mise à jour de la richesse (à étendre selon tes besoins)
        return tracker;
    }
}
