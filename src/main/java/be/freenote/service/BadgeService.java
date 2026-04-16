package be.freenote.service;

import be.freenote.entity.User;

public interface BadgeService {
    void checkAndAwardBadges(User user);
}
