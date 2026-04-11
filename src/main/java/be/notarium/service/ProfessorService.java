package be.notarium.service;

import be.notarium.dto.response.ProfessorResponse;

import java.util.List;

public interface ProfessorService {
    List<ProfessorResponse> getAll();
    ProfessorResponse create(String name);
    ProfessorResponse approve(Long id);
    List<ProfessorResponse> getPending();
}
