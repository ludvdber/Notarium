package be.freenote.service;

import be.freenote.dto.request.KofiWebhookPayload;

public interface KofiService {
    void processWebhook(KofiWebhookPayload payload);
}
