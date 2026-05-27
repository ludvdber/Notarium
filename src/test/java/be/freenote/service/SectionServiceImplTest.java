package be.freenote.service;

import be.freenote.dto.response.SectionResponse;
import be.freenote.entity.Course;
import be.freenote.entity.Document;
import be.freenote.entity.Section;
import be.freenote.exception.DuplicateResourceException;
import be.freenote.exception.ResourceNotFoundException;
import be.freenote.repository.DocumentRepository;
import be.freenote.repository.SectionRepository;
import be.freenote.service.impl.SectionServiceImpl;
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
class SectionServiceImplTest {

    @Mock private SectionRepository sectionRepository;
    @Mock private DocumentRepository documentRepository;
    @Mock private MinioService minioService;
    @Mock private MeilisearchService meilisearchService;
    @Mock private StatsService statsService;

    @InjectMocks private SectionServiceImpl sectionService;

    @Test
    void create_shouldReturnNewSectionWithZeroDocs() {
        Section saved = Section.builder().id(1L).name("Info").icon("💻").approved(true).build();
        when(sectionRepository.existsByNameIgnoreCase("Info")).thenReturn(false);
        when(sectionRepository.save(any(Section.class))).thenReturn(saved);

        SectionResponse response = sectionService.create("Info", "💻");

        assertThat(response.name()).isEqualTo("Info");
        assertThat(response.icon()).isEqualTo("💻");
        assertThat(response.documentCount()).isZero();
        assertThat(response.approved()).isTrue();
    }

    @Test
    void create_shouldRejectDuplicateName() {
        when(sectionRepository.existsByNameIgnoreCase("Info")).thenReturn(true);

        assertThatThrownBy(() -> sectionService.create("Info", null))
                .isInstanceOf(DuplicateResourceException.class);
        verify(sectionRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectEmptyName() {
        assertThatThrownBy(() -> sectionService.create("   ", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rename_shouldUpdateNameAndIcon() {
        Section section = Section.builder().id(1L).name("Old").icon("📚").approved(true).build();
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(sectionRepository.existsByNameIgnoreCase("New")).thenReturn(false);
        when(sectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(documentRepository.countBySectionId(1L)).thenReturn(3L);

        SectionResponse response = sectionService.rename(1L, "New", "🧠");

        assertThat(response.name()).isEqualTo("New");
        assertThat(response.icon()).isEqualTo("🧠");
        assertThat(response.documentCount()).isEqualTo(3);
    }

    @Test
    void rename_shouldAllowSameNameCaseChange() {
        Section section = Section.builder().id(1L).name("info").approved(true).build();
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(sectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(documentRepository.countBySectionId(1L)).thenReturn(0L);

        SectionResponse response = sectionService.rename(1L, "Info", null);

        assertThat(response.name()).isEqualTo("Info");
        verify(sectionRepository, never()).existsByNameIgnoreCase(anyString());
    }

    @Test
    void approve_shouldSetApprovedTrue() {
        Section section = Section.builder().id(1L).name("Compta").approved(false).courses(List.of()).build();
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(sectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(documentRepository.countBySectionId(1L)).thenReturn(0L);

        SectionResponse response = sectionService.approve(1L);

        assertThat(section.isApproved()).isTrue();
        assertThat(response.name()).isEqualTo("Compta");
    }

    @Test
    void approve_shouldThrowWhenNotFound() {
        when(sectionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionService.approve(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_shouldComputeDocCountFromSingleQuery() {
        Section section = Section.builder().id(1L).name("Info").build();
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(documentRepository.countBySectionId(1L)).thenReturn(8L);

        SectionResponse response = sectionService.getById(1L);

        assertThat(response.documentCount()).isEqualTo(8);
    }

    @Test
    void adminDelete_shouldCleanupMinioAndMeilisearchForEveryDoc() {
        Document doc1 = Document.builder().id(10L).fileKey("k1").build();
        Document doc2 = Document.builder().id(11L).fileKey("k2").build();
        Course course = Course.builder().id(100L).documents(List.of(doc1, doc2)).build();
        Section section = Section.builder().id(1L).name("Info").courses(List.of(course)).build();
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));

        sectionService.adminDelete(1L);

        verify(minioService).delete("k1");
        verify(minioService).delete("k2");
        verify(meilisearchService).deleteDocument(10L);
        verify(meilisearchService).deleteDocument(11L);
        verify(sectionRepository).delete(section);
        verify(statsService).invalidateCache();
    }

    @Test
    void adminDelete_shouldSkipMinioForDocWithoutFileKey() {
        Document doc = Document.builder().id(10L).fileKey(null).build();
        Course course = Course.builder().id(100L).documents(List.of(doc)).build();
        Section section = Section.builder().id(1L).name("Info").courses(List.of(course)).build();
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));

        sectionService.adminDelete(1L);

        verify(minioService, never()).delete(any());
        verify(meilisearchService).deleteDocument(10L);
    }
}
