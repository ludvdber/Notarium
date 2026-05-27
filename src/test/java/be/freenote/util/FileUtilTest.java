package be.freenote.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class FileUtilTest {

    // ---- sanitizeFileName ----

    @Test
    void shouldKeepSafeCharacters() {
        assertThat(FileUtil.sanitizeFileName("document.pdf")).isEqualTo("document.pdf");
        assertThat(FileUtil.sanitizeFileName("my-file_v2.pdf")).isEqualTo("my-file_v2.pdf");
    }

    @Test
    void shouldReplaceUnsafeCharacters() {
        assertThat(FileUtil.sanitizeFileName("my file (1).pdf")).isEqualTo("my_file__1_.pdf");
    }

    @Test
    void shouldReplaceSpecialCharacters() {
        assertThat(FileUtil.sanitizeFileName("doc<script>.pdf")).isEqualTo("doc_script_.pdf");
    }

    @Test
    void shouldReturnUnnamedWhenNull() {
        assertThat(FileUtil.sanitizeFileName(null)).isEqualTo("unnamed");
    }

    @Test
    void shouldHandleAccentedCharacters() {
        assertThat(FileUtil.sanitizeFileName("résumé.pdf")).isEqualTo("r_sum_.pdf");
    }
}
