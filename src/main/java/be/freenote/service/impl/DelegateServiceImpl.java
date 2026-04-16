package be.freenote.service.impl;

import be.freenote.dto.request.AssignDelegateRequest;
import be.freenote.dto.request.EndDelegateRequest;
import be.freenote.dto.response.DelegateHistoryResponse;
import be.freenote.dto.response.DelegateMember;
import be.freenote.dto.response.DelegateResponse;
import be.freenote.entity.DelegateHistory;
import be.freenote.entity.Section;
import be.freenote.entity.User;
import be.freenote.exception.DuplicateResourceException;
import be.freenote.exception.ResourceNotFoundException;
import be.freenote.mapper.DelegateMapper;
import be.freenote.repository.DelegateHistoryRepository;
import be.freenote.repository.SectionRepository;
import be.freenote.repository.UserRepository;
import be.freenote.service.DelegateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DelegateServiceImpl implements DelegateService {

    private final DelegateHistoryRepository delegateHistoryRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
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
    public List<DelegateHistoryResponse> getHistory(Long userId) {
        return delegateHistoryRepository.findByUserId(userId).stream()
                .sorted(Comparator.comparing(DelegateHistory::getStartDate).reversed())
                .map(delegateMapper::toHistoryResponse)
                .toList();
    }

    @Override
    public List<DelegateMember> getAllMandates() {
        return delegateHistoryRepository.findAll().stream()
                .sorted(Comparator.comparing(DelegateHistory::getStartDate).reversed())
                .map(delegateMapper::toMember)
                .toList();
    }

    @Override
    @Transactional
    public DelegateMember assignDelegate(AssignDelegateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", request.getSectionId()));

        // A user can only be an active delegate for one section at a time
        delegateHistoryRepository.findByUserIdAndEndDateIsNull(user.getId())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "User '" + user.getUsername() + "' is already an active delegate for section '"
                                    + existing.getSection().getName() + "'. End that mandate first.");
                });

        DelegateHistory dh = DelegateHistory.builder()
                .user(user)
                .section(section)
                .startDate(request.getStartDate())
                .build();

        return delegateMapper.toMember(delegateHistoryRepository.save(dh));
    }

    @Override
    @Transactional
    public DelegateMember endDelegate(Long historyId, EndDelegateRequest request) {
        DelegateHistory dh = delegateHistoryRepository.findById(historyId)
                .orElseThrow(() -> new ResourceNotFoundException("DelegateHistory", "id", historyId));

        if (dh.getEndDate() != null) {
            throw new IllegalArgumentException("This mandate is already ended (end date: " + dh.getEndDate() + ")");
        }

        if (request.getEndDate().isBefore(dh.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date (" + dh.getStartDate() + ")");
        }

        dh.setEndDate(request.getEndDate());
        return delegateMapper.toMember(delegateHistoryRepository.save(dh));
    }

    @Override
    @Transactional
    public void deleteMandate(Long historyId) {
        if (!delegateHistoryRepository.existsById(historyId)) {
            throw new ResourceNotFoundException("DelegateHistory", "id", historyId);
        }
        delegateHistoryRepository.deleteById(historyId);
    }
}
