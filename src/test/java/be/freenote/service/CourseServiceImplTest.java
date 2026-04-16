package be.freenote.service;

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
import be.freenote.service.impl.CourseServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock private CourseRepository courseRepository;
    @Mock private SectionRepository sectionRepository;
    @Mock private UserRepository userRepository;
    @Mock private DocumentRepository documentRepository;
    @Mock private CourseMapper courseMapper;

    @InjectMocks private CourseServiceImpl courseService;

    @Test
    void shouldCreateCourse() {
        Section sec = Section.builder().id(10L).name("IT").build();
        User user = User.builder().id(1L).username("creator").build();

        when(sectionRepository.findById(10L)).thenReturn(Optional.of(sec));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> {
            Course c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        CourseResponse resp = new CourseResponse(1L, "Java", "IT", 0);
        when(courseMapper.toResponse(any(Course.class), eq(0L))).thenReturn(resp);

        CreateCourseRequest req = new CreateCourseRequest();
        req.setName("Java");
        req.setSectionId(10L);

        CourseResponse result = courseService.create(req, 1L);

        assertThat(result.name()).isEqualTo("Java");
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void shouldApproveCourse() {
        Course course = Course.builder().id(1L).name("Java").approved(false)
                .section(Section.builder().id(10L).name("IT").build()).build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.save(course)).thenReturn(course);
        when(documentRepository.countByCourseId(1L)).thenReturn(5L);

        CourseResponse resp = new CourseResponse(1L, "Java", "IT", 5);
        when(courseMapper.toResponse(course, 5L)).thenReturn(resp);

        courseService.approve(1L);

        assertThat(course.isApproved()).isTrue();
    }

    @Test
    void shouldReturnOnlyApprovedCoursesForSection() {
        Course approved = Course.builder().id(1L).name("Java").approved(true)
                .section(Section.builder().id(10L).name("IT").build()).build();

        when(sectionRepository.existsById(10L)).thenReturn(true);
        List<Object[]> rows = new java.util.ArrayList<>();
        rows.add(new Object[]{approved, 3L});
        when(courseRepository.findApprovedBySectionIdWithDocCount(10L)).thenReturn(rows);

        CourseResponse resp = new CourseResponse(1L, "Java", "IT", 3);
        when(courseMapper.toResponse(approved, 3L)).thenReturn(resp);

        List<CourseResponse> result = courseService.getBySectionId(10L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Java");
    }

    @Test
    void shouldReturnPendingCourses() {
        Course pending = Course.builder().id(2L).name("C#").approved(false)
                .section(Section.builder().id(10L).name("IT").build()).build();

        when(courseRepository.findByApprovedFalse()).thenReturn(List.of(pending));

        CourseResponse resp = new CourseResponse(2L, "C#", "IT", 0);
        when(courseMapper.toResponse(pending, 0L)).thenReturn(resp);

        List<CourseResponse> result = courseService.getPending();

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldThrowNotFoundWhenApprovingNonExistentCourse() {
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.approve(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
