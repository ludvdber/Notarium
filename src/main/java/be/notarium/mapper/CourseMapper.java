package be.notarium.mapper;

import be.notarium.dto.response.CourseResponse;
import be.notarium.entity.Course;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    @Mapping(target = "sectionName", source = "course.section.name")
    @Mapping(target = "documentCount", source = "docCount")
    CourseResponse toResponse(Course course, long docCount);
}
