package be.freenote.service.impl;

import be.freenote.dto.response.ProfessorResponse;
import be.freenote.entity.Professor;
import be.freenote.exception.ResourceNotFoundException;
import be.freenote.mapper.ProfessorMapper;
import be.freenote.repository.ProfessorRepository;
import be.freenote.service.ProfessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfessorServiceImpl implements ProfessorService {

    private final ProfessorRepository professorRepository;
    private final ProfessorMapper professorMapper;

    @Override
    public List<ProfessorResponse> getAll() {
        return professorRepository.findAll().stream()
                .filter(Professor::isApproved)
                .map(professorMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProfessorResponse create(String name) {
        Professor professor = Professor.builder()
                .name(name)
                .build();
        return professorMapper.toResponse(professorRepository.save(professor));
    }

    @Override
    @Transactional
    public ProfessorResponse approve(Long id) {
        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professor", "id", id));
        professor.setApproved(true);
        return professorMapper.toResponse(professorRepository.save(professor));
    }

    @Override
    public List<ProfessorResponse> getPending() {
        return professorRepository.findByApprovedFalse().stream()
                .map(professorMapper::toResponse)
                .toList();
    }
}
