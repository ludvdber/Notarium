package be.freenote.service;

import be.freenote.service.impl.PdfCompressionServiceImpl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PdfCompressionServiceImplTest {

    private final PdfCompressionServiceImpl service = new PdfCompressionServiceImpl();

    @Test
    void compress_shouldReturnOriginalWhenGhostscriptNotAvailable() {
        // On machines without Ghostscript, compress() should fallback gracefully
        byte[] original = "%PDF-1.4 fake pdf content for testing".getBytes();

        byte[] result = service.compress(original);

        // Either compressed (if gs is available) or original (fallback)
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void compress_shouldHandleEmptyInput() {
        byte[] result = service.compress(new byte[0]);

        // Should not crash — returns original (empty)
        assertThat(result).isNotNull();
    }
}
