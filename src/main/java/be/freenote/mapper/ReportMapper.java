package be.freenote.mapper;

import be.freenote.dto.response.ReportResponse;
import be.freenote.entity.Report;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReportMapper {

    @Mapping(target = "documentId", source = "document.id")
    @Mapping(target = "documentTitle", source = "document.title")
    @Mapping(target = "reporterUsername", source = "user.username")
    @Mapping(target = "createdAt", source = "createdAt")
    ReportResponse toResponse(Report report);
}
