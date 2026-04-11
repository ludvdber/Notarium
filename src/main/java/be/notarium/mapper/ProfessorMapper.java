package be.notarium.mapper;

import be.notarium.dto.response.ProfessorResponse;
import be.notarium.entity.Professor;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfessorMapper {
    ProfessorResponse toResponse(Professor professor);
}
