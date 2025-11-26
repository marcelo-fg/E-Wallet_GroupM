package org.groupm.ewallet.mapper;

import org.groupm.ewallet.model.WealthTracker;
import org.groupm.ewallet.dto.WealthTrackerDTO;
import org.groupm.ewallet.model.User;

/**
 * Mapper pour convertir WealthTracker ↔ WealthTrackerDTO.
 */
public final class WealthTrackerMapper {

    private WealthTrackerMapper() {}

    // ============================================================
    //                      ENTITY → DTO
    // ============================================================

    public static WealthTrackerDTO toDto(WealthTracker tracker) {
        if (tracker == null) return null;

        WealthTrackerDTO dto = new WealthTrackerDTO();
        User user = tracker.getUser();

        dto.setUserID(user.getUserID());
        dto.setTotalWealthUsd(tracker.getTotalWealthUsd());
        dto.setTotalWealthChf(tracker.getTotalWealthChf());
        dto.setGrowthRate(tracker.getGrowthRate());

        return dto;
    }

    // ============================================================
    //                      DTO → ENTITY
    // ============================================================

    /**
     * Conversion inverse.
     * Nécessite l'entité User → cohérent avec ton modèle,
     * car WealthTracker suit un utilisateur.
     */
    public static WealthTracker toEntity(WealthTrackerDTO dto, User user) {
        if (dto == null || user == null) {
            return null;
        }

        // Le WealthTracker doit être attaché à un User existant
        return new WealthTracker(user);
    }
}