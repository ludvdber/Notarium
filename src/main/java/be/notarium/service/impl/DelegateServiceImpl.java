package be.notarium.service.impl;

import be.notarium.dto.response.DelegateResponse;
import be.notarium.entity.DelegateHistory;
import be.notarium.mapper.DelegateMapper;
import be.notarium.repository.DelegateHistoryRepository;
import be.notarium.service.DelegateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DelegateServiceImpl implements DelegateService {

    private final DelegateHistoryRepository delegateHistoryRepository;
    private final DelegateMapper delegateMapper;

    @Override
    public List<DelegateResponse> getActiveDelegates() {
        List<DelegateHistory> active = delegateHistoryRepository.findByEndDateIsNull();

        Map<String, List<DelegateHistory>> bySection = active.stream()
                .collect(Collectors.groupingBy(dh -> dh.getSection().getName()));

        return bySection.entrySet().stream()
                .map(entry -> new DelegateResponse(
                        entry.getKey(),
                        entry.getValue().getFirst().getSection().getIcon(),
                        entry.getValue().stream()
                                .map(delegateMapper::toMember)
                                .toList()
                ))
                .toList();
    }

    @Override
    public List<DelegateHistory> getHistory(Long userId) {
        return delegateHistoryRepository.findByUserId(userId);
    }
}
