package be.freenote.service;

import be.freenote.dto.response.DonationResponse;
import be.freenote.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface DonationService {

    PageResponse<DonationResponse> listAll(Pageable pageable);

    /**
     * Grants `days` days of ad-free status to the given user. If the user already has an
     * active ad-free period, the new grant extends it; otherwise it starts now.
     * Records a synthetic Donation entry with amount=0 and a transaction id prefixed
     * "MANUAL-{adminUserId}-…" so the audit trail shows admin grants alongside Ko-fi donations.
     */
    DonationResponse grantAdFree(Long targetUserId, int days, Long actingAdminId);
}
