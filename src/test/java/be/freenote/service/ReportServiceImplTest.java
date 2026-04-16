package be.freenote.service;

import be.freenote.dto.request.ReportRequest;
import be.freenote.entity.Document;
import be.freenote.entity.Report;
import be.freenote.entity.User;
import be.freenote.enums.Category;
import be.freenote.enums.ReportStatus;
import be.freenote.exception.ResourceNotFoundException;
import be.freenote.mapper.ReportMapper;
import be.freenote.repository.DocumentRepository;
import be.freenote.repository.ReportRepository;
import be.freenote.repository.UserRepository;
import be.freenote.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock private ReportRepository reportRepository;
    @Mock private DocumentRepository documentRepository;
    @Mock private UserRepository userRepository;
    @Mock private ReportMapper reportMapper;

    @InjectMocks private ReportServiceImpl reportService;

    @Test
    void shouldCreateReport() {
        User user = User.builder().id(1L).username("reporter").build();
        Document doc = Document.builder().id(100L).title("Doc").category(Category.SYNTHESE)
                .fileKey("k").fileSize(100L).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(documentRepository.findById(100L)).thenReturn(Optional.of(doc));

        ReportRequest req = new ReportRequest();
        req.setReason("Contenu inapproprié");

        reportService.create(1L, 100L, req);

        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository).save(captor.capture());
        assertThat(captor.getValue().getReason()).isEqualTo("Contenu inapproprié");
        assertThat(captor.getValue().getDocument()).isEqualTo(doc);
        assertThat(captor.getValue().getUser()).isEqualTo(user);
    }

    @Test
    void shouldThrowWhenReportingNonExistentDocument() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        ReportRequest req = new ReportRequest();
        req.setReason("test");

        assertThatThrownBy(() -> reportService.create(1L, 999L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldResolveReport() {
        Report report = Report.builder().id(1L).status(ReportStatus.PENDING).reason("r").build();
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(reportRepository.save(report)).thenReturn(report);

        reportService.resolve(1L);

        assertThat(report.getStatus()).isEqualTo(ReportStatus.RESOLVED);
    }

    @Test
    void shouldDismissReport() {
        Report report = Report.builder().id(1L).status(ReportStatus.PENDING).reason("r").build();
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(reportRepository.save(report)).thenReturn(report);

        reportService.dismiss(1L);

        assertThat(report.getStatus()).isEqualTo(ReportStatus.DISMISSED);
    }

    @Test
    void shouldThrowWhenResolvingNonExistentReport() {
        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.resolve(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
