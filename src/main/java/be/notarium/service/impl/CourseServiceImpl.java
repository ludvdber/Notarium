package be.notarium.service.impl;

import be.notarium.dto.request.CreateCourseRequest;
import be.notarium.dto.response.CourseResponse;
import be.notarium.entity.Course;
import be.notarium.entity.Section;
import be.notarium.entity.User;
import be.notarium.exception.ResourceNotFoundException;
import be.notarium.mapper.CourseMapper;
import be.notarium.repository.CourseRepository;
import be.notarium.repository.DocumentRepository;
import be.notarium.repository.SectionRepository;
import be.notarium.repository.UserRepository;
import be.notarium.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final CourseMapper courseMapper;

    @Override
    public List<CourseResponse> getBySectionId(Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));
        return section.getCourses().stream()
                .filter(Course::isApproved)
                .map(c -> courseMapper.toResponse(c, documentRepository.countByCourseId(c.getId())))
                .toList();
    }

    @Override
    public CourseResponse getById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        return courseMapper.toResponse(course, documentRepository.countByCourseId(id));
    }

    @Override
    @Transactional
    public CourseResponse create(CreateCourseRequest request, Long userId) {
        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", request.getSectionId()));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Course course = Course.builder()
                .name(request.getName())
                .section(section)
                .createdBy(user)
                .build();

        return courseMapper.toResponse(courseRepository.save(course), 0);
    }

    @Override
    @Transactional
    public CourseResponse approve(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        course.setApproved(true);
        return courseMapper.toResponse(courseRepository.save(course), documentRepository.countByCourseId(id));
    }

    @Override
    public List<CourseResponse> getPending() {
        return courseRepository.findByApprovedFalse().stream()
                .map(c -> courseMapper.toResponse(c, documentRepository.countByCourseId(c.getId())))
                .toList();
    }
}
