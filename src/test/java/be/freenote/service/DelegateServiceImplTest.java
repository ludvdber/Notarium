package be.freenote.service;

import be.freenote.dto.request.AssignDelegateRequest;
import be.freenote.dto.request.EndDelegateRequest;
import be.freenote.dto.response.DelegateMember;
import be.freenote.dto.response.DelegateHistoryResponse;
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
import be.freenote.service.impl.DelegateServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DelegateServiceImplTest {

    @Mock private DelegateHistoryRepository delegateHistoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private SectionRepository sectionRepository;
    @Mock private DelegateMapper delegateMapper;

    @InjectMocks private DelegateServiceImpl delegateService;

    private Section section(Long id, String name) {
        return Section.builder().id(id).name(name).icon("icon").build();
    }

    // ---- assignDelegate ----

    @Test
    void shouldAssignDelegateSuccessfully() {
        User user = User.builder().id(1L).username("delegate").build();
        Section sec = section(10L, "IT");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(sectionRepository.findById(10L)).thenReturn(Optional.of(sec));
        when(delegateHistoryRepository.findByUserIdAndEndDateIsNull(1L)).thenReturn(Optional.empty());
        when(delegateHistoryRepository.save(any(DelegateHistory.class))).thenAnswer(inv -> {
            DelegateHistory dh = inv.getArgument(0);
            dh.setId(1L);
            return dh;
        });

        DelegateMember member = new DelegateMember(1L, 1L, null, "delegate", null,
                LocalDate.of(2026, 1, 1), null);
        when(delegateMapper.toMember(any(DelegateHistory.class))).thenReturn(member);

        AssignDelegateRequest req = new AssignDelegateRequest();
        req.setUserId(1L);
        req.setSectionId(10L);
        req.setStartDate(LocalDate.of(2026, 1, 1));

        DelegateMember result = delegateService.assignDelegate(req);

        assertThat(result.username()).isEqualTo("delegate");
        verify(delegateHistoryRepository).save(any(DelegateHistory.class));
    }

    @Test
    void shouldThrowDuplicateWhenUserAlreadyActiveDelegate() {
        User user = User.builder().id(1L).username("delegate").build();
        Section sec = section(10L, "IT");
        Section existingSec = section(20L, "Compta");

        DelegateHistory existing = DelegateHistory.builder()
                .id(5L).user(user).section(existingSec)
                .startDate(LocalDate.of(2025, 9, 1)).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(sectionRepository.findById(10L)).thenReturn(Optional.of(sec));
        when(delegateHistoryRepository.findByUserIdAndEndDateIsNull(1L))
                .thenReturn(Optional.of(existing));

        AssignDelegateRequest req = new AssignDelegateRequest();
        req.setUserId(1L);
        req.setSectionId(10L);
        req.setStartDate(LocalDate.now());

        assertThatThrownBy(() -> delegateService.assignDelegate(req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already an active delegate");
    }

    @Test
    void shouldThrowNotFoundWhenUserDoesNotExist() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        AssignDelegateRequest req = new AssignDelegateRequest();
        req.setUserId(999L);
        req.setSectionId(10L);
        req.setStartDate(LocalDate.now());

        assertThatThrownBy(() -> delegateService.assignDelegate(req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowNotFoundWhenSectionDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(sectionRepository.findById(999L)).thenReturn(Optional.empty());

        AssignDelegateRequest req = new AssignDelegateRequest();
        req.setUserId(1L);
        req.setSectionId(999L);
        req.setStartDate(LocalDate.now());

        assertThatThrownBy(() -> delegateService.assignDelegate(req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---- endDelegate ----

    @Test
    void shouldEndDelegateSuccessfully() {
        DelegateHistory dh = DelegateHistory.builder()
                .id(1L).startDate(LocalDate.of(2026, 1, 1)).endDate(null).build();

        when(delegateHistoryRepository.findById(1L)).thenReturn(Optional.of(dh));
        when(delegateHistoryRepository.save(dh)).thenReturn(dh);

        DelegateMember member = new DelegateMember(1L, 1L, null, "u", null,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1));
        when(delegateMapper.toMember(dh)).thenReturn(member);

        EndDelegateRequest req = new EndDelegateRequest();
        req.setEndDate(LocalDate.of(2026, 6, 1));

        DelegateMember result = delegateService.endDelegate(1L, req);

        assertThat(dh.getEndDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(result.endDate()).isEqualTo(LocalDate.of(2026, 6, 1));
    }

    @Test
    void shouldThrowWhenEndDateBeforeStartDate() {
        DelegateHistory dh = DelegateHistory.builder()
                .id(1L).startDate(LocalDate.of(2026, 6, 1)).endDate(null).build();

        when(delegateHistoryRepository.findById(1L)).thenReturn(Optional.of(dh));

        EndDelegateRequest req = new EndDelegateRequest();
        req.setEndDate(LocalDate.of(2026, 1, 1)); // before start

        assertThatThrownBy(() -> delegateService.endDelegate(1L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("before start date");
    }

    @Test
    void shouldThrowWhenMandateAlreadyEnded() {
        DelegateHistory dh = DelegateHistory.builder()
                .id(1L).startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 6, 1)).build();

        when(delegateHistoryRepository.findById(1L)).thenReturn(Optional.of(dh));

        EndDelegateRequest req = new EndDelegateRequest();
        req.setEndDate(LocalDate.of(2026, 1, 1));

        assertThatThrownBy(() -> delegateService.endDelegate(1L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already ended");
    }

    // ---- deleteMandate ----

    @Test
    void shouldDeleteMandateSuccessfully() {
        when(delegateHistoryRepository.existsById(1L)).thenReturn(true);

        delegateService.deleteMandate(1L);

        verify(delegateHistoryRepository).deleteById(1L);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonExistentMandate() {
        when(delegateHistoryRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> delegateService.deleteMandate(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---- getHistory ----

    @Test
    void shouldReturnHistorySortedByStartDateDesc() {
        User user = User.builder().id(1L).build();
        Section sec = section(10L, "IT");

        DelegateHistory dh1 = DelegateHistory.builder()
                .id(1L).user(user).section(sec)
                .startDate(LocalDate.of(2024, 1, 1)).endDate(LocalDate.of(2024, 6, 1)).build();
        DelegateHistory dh2 = DelegateHistory.builder()
                .id(2L).user(user).section(sec)
                .startDate(LocalDate.of(2025, 1, 1)).endDate(null).build();

        when(delegateHistoryRepository.findByUserId(1L)).thenReturn(List.of(dh1, dh2));

        DelegateHistoryResponse r1 = new DelegateHistoryResponse(1L, "IT",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 1), false);
        DelegateHistoryResponse r2 = new DelegateHistoryResponse(2L, "IT",
                LocalDate.of(2025, 1, 1), null, true);
        when(delegateMapper.toHistoryResponse(dh2)).thenReturn(r2);
        when(delegateMapper.toHistoryResponse(dh1)).thenReturn(r1);

        List<DelegateHistoryResponse> result = delegateService.getHistory(1L);

        assertThat(result).hasSize(2);
        // Most recent first
        assertThat(result.get(0).startDate()).isAfter(result.get(1).startDate());
    }

    // ---- getActiveDelegates ----

    @Test
    void shouldGroupActiveDelegatesBySection() {
        Section secIT = section(10L, "IT");
        Section secCompta = section(20L, "Compta");
        User u1 = User.builder().id(1L).username("d1").build();
        User u2 = User.builder().id(2L).username("d2").build();

        DelegateHistory dh1 = DelegateHistory.builder()
                .id(1L).user(u1).section(secIT).startDate(LocalDate.now()).build();
        DelegateHistory dh2 = DelegateHistory.builder()
                .id(2L).user(u2).section(secCompta).startDate(LocalDate.now()).build();

        when(delegateHistoryRepository.findByEndDateIsNull()).thenReturn(List.of(dh1, dh2));

        DelegateMember m1 = new DelegateMember(1L, 1L, null, "d1", null, LocalDate.now(), null);
        DelegateMember m2 = new DelegateMember(2L, 2L, null, "d2", null, LocalDate.now(), null);
        when(delegateMapper.toMember(dh1)).thenReturn(m1);
        when(delegateMapper.toMember(dh2)).thenReturn(m2);

        List<DelegateResponse> result = delegateService.getActiveDelegates();

        assertThat(result).hasSize(2);
    }
}
