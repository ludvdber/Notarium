package be.freenote.service.impl;

import be.freenote.dto.request.CreateCourseRequest;
import be.freenote.dto.response.CourseResponse;
import be.freenote.entity.Course;
import be.freenote.entity.Section;
import be.freenote.entity.User;
import be.freenote.exception.ResourceNotFoundException;
import be.freenote.mapper.CourseMapper;
import be.freenote.repository.CourseRepository;
import be.freenote.repository.DocumentRepository;
import be.freenote.repository.SectionRepository;
import be.freenote.repository.UserRepository;
import be.freenote.service.CourseService;
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
