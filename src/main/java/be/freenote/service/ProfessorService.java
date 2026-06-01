package be.freenote.service;

import be.freenote.dto.response.ProfessorResponse;

import java.util.List;

public interface ProfessorService {
    List<ProfessorResponse> getAll();
    ProfessorResponse create(String name);
    /** Admin: create a professor approved immediately (bypasses the pending queue). */
    ProfessorResponse adminCreate(String name);
    ProfessorResponse approve(Long id);
    List<ProfessorResponse> getPending();
}
