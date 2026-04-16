package be.freenote.service;

import be.freenote.dto.request.AssignDelegateRequest;
import be.freenote.dto.request.EndDelegateRequest;
import be.freenote.dto.response.DelegateHistoryResponse;
import be.freenote.dto.response.DelegateMember;
import be.freenote.dto.response.DelegateResponse;

import java.util.List;

public interface DelegateService {
    List<DelegateResponse> getActiveDelegates();
    List<DelegateHistoryResponse> getHistory(Long userId);
    List<DelegateMember> getAllMandates();
    DelegateMember assignDelegate(AssignDelegateRequest request);
    DelegateMember endDelegate(Long historyId, EndDelegateRequest request);
    void deleteMandate(Long historyId);
}
