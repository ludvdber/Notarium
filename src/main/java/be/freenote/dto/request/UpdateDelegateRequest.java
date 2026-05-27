package be.freenote.dto.request;

import lombok.Data;

import java.time.LocalDate;

/**
 * Admin-level edit of a delegate mandate. Both fields are optional: send only the one
 * you want to change. Sending `endDate=null` explicitly reopens a closed mandate — but
 * only if the user doesn't already have another active one.
 */
@Data
public class UpdateDelegateRequest {

    private LocalDate startDate;

    private LocalDate endDate;

    /** True when the payload explicitly sets endDate to null to reopen the mandate. */
    private boolean clearEndDate;
}
