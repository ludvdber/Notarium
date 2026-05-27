package be.freenote.service;

import be.freenote.dto.request.CreateCourseRequest;
import be.freenote.dto.response.CourseResponse;
import be.freenote.entity.Course;
import be.freenote.entity.Document;
import be.freenote.entity.Section;
import be.freenote.entity.User;
import be.freenote.exception.DuplicateResourceException;
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
    @Mock private MinioService minioService;
    @Mock private MeilisearchService meilisearchService;
    @Mock private StatsService statsService;

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

        CourseResponse resp = new CourseResponse(1L, "Java", 10L, "IT", 0, true);
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

        CourseResponse resp = new CourseResponse(1L, "Java", 10L, "IT", 5, true);
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

        CourseResponse resp = new CourseResponse(1L, "Java", 10L, "IT", 3, true);
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

        CourseResponse resp = new CourseResponse(2L, "C#", 10L, "IT", 0, false);
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

    @Test
    void adminCreate_shouldSkipPendingQueue() {
        Section sec = Section.builder().id(10L).name("IT").build();
        when(sectionRepository.findById(10L)).thenReturn(Optional.of(sec));
        when(courseRepository.existsBySectionIdAndNameIgnoreCase(10L, "Kotlin")).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> {
            Course c = inv.getArgument(0);
            c.setId(42L);
            return c;
        });
        CourseResponse resp = new CourseResponse(42L, "Kotlin", 10L, "IT", 0, true);
        when(courseMapper.toResponse(any(Course.class), eq(0L))).thenReturn(resp);

        CreateCourseRequest req = new CreateCourseRequest();
        req.setName("Kotlin");
        req.setSectionId(10L);

        CourseResponse result = courseService.adminCreate(req);

        assertThat(result.approved()).isTrue();
        verify(courseRepository).save(argThat(Course::isApproved));
    }

    @Test
    void adminCreate_shouldRejectDuplicate() {
        Section sec = Section.builder().id(10L).name("IT").build();
        when(sectionRepository.findById(10L)).thenReturn(Optional.of(sec));
        when(courseRepository.existsBySectionIdAndNameIgnoreCase(10L, "Java")).thenReturn(true);

        CreateCourseRequest req = new CreateCourseRequest();
        req.setName("Java");
        req.setSectionId(10L);

        assertThatThrownBy(() -> courseService.adminCreate(req))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void rename_shouldUpdateName() {
        Section sec = Section.builder().id(10L).name("IT").build();
        Course course = Course.builder().id(1L).name("Old").section(sec).approved(true).build();
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.existsBySectionIdAndNameIgnoreCase(10L, "New")).thenReturn(false);
        when(courseRepository.save(course)).thenReturn(course);
        when(documentRepository.countByCourseId(1L)).thenReturn(2L);
        CourseResponse resp = new CourseResponse(1L, "New", 10L, "IT", 2, true);
        when(courseMapper.toResponse(course, 2L)).thenReturn(resp);

        CourseResponse result = courseService.rename(1L, "New");

        assertThat(course.getName()).isEqualTo("New");
        assertThat(result.name()).isEqualTo("New");
    }

    @Test
    void adminDelete_shouldCleanupMinioAndMeilisearchForEveryDoc() {
        Document doc1 = Document.builder().id(10L).fileKey("k1").build();
        Document doc2 = Document.builder().id(11L).fileKey("k2").build();
        Course course = Course.builder().id(1L).name("Algo").documents(List.of(doc1, doc2)).build();
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        courseService.adminDelete(1L);

        verify(minioService).delete("k1");
        verify(minioService).delete("k2");
        verify(meilisearchService).deleteDocument(10L);
        verify(meilisearchService).deleteDocument(11L);
        verify(courseRepository).delete(course);
        verify(statsService).invalidateCache();
    }
}
