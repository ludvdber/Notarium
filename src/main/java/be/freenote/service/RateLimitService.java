package be.freenote.service;

public interface RateLimitService {
    boolean isAllowed(String key, int max, long windowSeconds);
}
