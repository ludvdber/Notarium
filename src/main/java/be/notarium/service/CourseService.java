package be.notarium.service;

import be.notarium.dto.request.CreateCourseRequest;
import be.notarium.dto.response.CourseResponse;

import java.util.List;

public interface CourseService {
    List<CourseResponse> getBySectionId(Long sectionId);
    CourseResponse getById(Long id);
    CourseResponse create(CreateCourseRequest request, Long userId);
    CourseResponse approve(Long id);
    List<CourseResponse> getPending();
}
