package be.notarium.mapper;

import be.notarium.dto.response.DocumentResponse;
import be.notarium.entity.Document;
import be.notarium.entity.Tag;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(target = "courseName", source = "course.name")
    @Mapping(target = "sectionName", source = "course.section.name")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "authorName", expression = "java(mapAuthorName(document))")
    @Mapping(target = "professorName", source = "professor.name")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "averageRating", expression = "java(document.getAverageRating().doubleValue())")
    @Mapping(target = "downloadCount", source = "downloadCount")
    DocumentResponse toResponse(Document document);

    default String mapAuthorName(Document document) {
        if (document.isAnonymous() || document.getUser() == null) {
            return "Anonyme";
        }
        return document.getUser().getUsername();
    }

    default List<String> mapTags(List<Tag> tags) {
        if (tags == null) {
            return List.of();
        }
        return tags.stream().map(Tag::getLabel).toList();
    }
}
