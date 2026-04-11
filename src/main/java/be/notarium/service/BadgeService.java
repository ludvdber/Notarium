package be.notarium.service;

import be.notarium.entity.User;

public interface BadgeService {
    void checkAndAwardBadges(User user);
}
