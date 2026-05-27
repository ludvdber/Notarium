package be.freenote.service;

import be.freenote.dto.response.DocumentResponse;

import java.util.List;

public interface RecentDocsService {
    /** Records that the given user just opened `docId`. No-op if either is null. */
    void recordVisit(Long userId, Long docId);

    /** Returns the last N verified documents the user has opened, most recent first. */
    List<DocumentResponse> getRecent(Long userId, int max);
}
