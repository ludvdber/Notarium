package be.freenote.service;

import be.freenote.dto.response.SectionResponse;
import be.freenote.entity.Course;
import be.freenote.entity.Section;
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

    @InjectMocks private SectionServiceImpl sectionService;

    @Test
    void create_shouldReturnNewSectionWithZeroDocs() {
        Section saved = Section.builder().id(1L).name("Info").icon("💻").build();
        when(sectionRepository.save(any(Section.class))).thenReturn(saved);

        SectionResponse response = sectionService.create("Info", "💻");

        assertThat(response.name()).isEqualTo("Info");
        assertThat(response.icon()).isEqualTo("💻");
        assertThat(response.documentCount()).isZero();
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
}
