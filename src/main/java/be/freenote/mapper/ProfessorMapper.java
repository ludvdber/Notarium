package be.freenote.mapper;

import be.freenote.dto.response.ProfessorResponse;
import be.freenote.entity.Professor;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfessorMapper {
    ProfessorResponse toResponse(Professor professor);
}
