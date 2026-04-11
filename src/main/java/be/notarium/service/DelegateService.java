package be.notarium.service;

import be.notarium.dto.response.DelegateResponse;
import be.notarium.entity.DelegateHistory;

import java.util.List;

public interface DelegateService {
    List<DelegateResponse> getActiveDelegates();
    List<DelegateHistory> getHistory(Long userId);
}
