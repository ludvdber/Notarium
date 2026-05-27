package be.freenote.controller;

import be.freenote.dto.request.KofiWebhookPayload;
import be.freenote.security.ratelimit.RateLimit;
import be.freenote.service.KofiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "External service webhooks")
public class KofiWebhookController {

    private final KofiService kofiService;
    private final ObjectMapper objectMapper;

    @PostMapping("/kofi")
    @RateLimit(max = 30, window = 60)
    @Operation(summary = "Ko-fi webhook",
               description = "Receives donation notifications from Ko-fi. Public endpoint, verified by token.")
    public ResponseEntity<Void> handleKofiWebhook(@RequestParam(value = "data", required = false) String data) {
        if (data != null && !data.isBlank()) {
            try {
                KofiWebhookPayload payload = objectMapper.readValue(data, KofiWebhookPayload.class);
                kofiService.processWebhook(payload);
            } catch (Exception e) {
                log.debug("Ko-fi webhook parsing failed: {}", e.getMessage());
            }
        }
        // Always return 200 so Ko-fi doesn't retry endlessly
        return ResponseEntity.ok().build();
    }
}
