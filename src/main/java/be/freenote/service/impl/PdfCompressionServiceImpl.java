package be.freenote.service.impl;

import be.freenote.service.PdfCompressionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class PdfCompressionServiceImpl implements PdfCompressionService {

    @Override
    public byte[] compress(byte[] pdfBytes) {
        Path inputFile = null;
        Path outputFile = null;
        try {
            inputFile = Files.createTempFile("freenote-input-", ".pdf");
            outputFile = Files.createTempFile("freenote-output-", ".pdf");
            Files.write(inputFile, pdfBytes);

            ProcessBuilder pb = new ProcessBuilder(
                    "gs",
                    "-sDEVICE=pdfwrite",
                    "-dCompatibilityLevel=1.4",
                    "-dPDFSETTINGS=/ebook",
                    "-dNOPAUSE",
                    "-dQUIET",
                    "-dBATCH",
                    "-sOutputFile=" + outputFile.toAbsolutePath(),
                    inputFile.toAbsolutePath().toString()
            );
            pb.redirectErrorStream(true);

            Process process = pb.start();
            boolean finished = process.waitFor(60, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                log.warn("Ghostscript timed out after 60s, returning original");
                return pdfBytes;
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.warn("Ghostscript compression failed with exit code {}, returning original", exitCode);
                return pdfBytes;
            }

            byte[] compressed = Files.readAllBytes(outputFile);

            if (compressed.length >= pdfBytes.length) {
                log.info("Compressed PDF is not smaller, returning original ({} >= {} bytes)",
                        compressed.length, pdfBytes.length);
                return pdfBytes;
            }

            log.info("PDF compressed from {} to {} bytes ({}% reduction)",
                    pdfBytes.length, compressed.length,
                    Math.round((1.0 - (double) compressed.length / pdfBytes.length) * 100));
            return compressed;

        } catch (IOException | InterruptedException e) {
            log.warn("PDF compression failed, returning original: {}", e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return pdfBytes;
        } finally {
            deleteTempFile(inputFile);
            deleteTempFile(outputFile);
        }
    }

    private void deleteTempFile(Path path) {
        if (path != null) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                log.warn("Failed to delete temp file: {}", path);
            }
        }
    }
}
