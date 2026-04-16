package be.freenote.service.impl;

import be.freenote.exception.PayloadTooLargeException;
import be.freenote.service.PdfCompressionService;
import be.freenote.service.PdfValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class PdfValidationServiceImpl implements PdfValidationService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final byte[] PDF_MAGIC = {0x25, 0x50, 0x44, 0x46, 0x2D}; // %PDF-

    private final PdfCompressionService pdfCompressionService;

    @Override
    public byte[] validateAndCompress(MultipartFile file) {
        if (!PDF_CONTENT_TYPE.equals(file.getContentType())) {
            throw new IllegalArgumentException("Only PDF files are accepted");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new PayloadTooLargeException("File size exceeds the 10 MB limit");
        }

        byte[] pdfBytes;
        try {
            pdfBytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }

        if (pdfBytes.length < 5 || !Arrays.equals(PDF_MAGIC, 0, 5, pdfBytes, 0, 5)) {
            throw new IllegalArgumentException("Le fichier n'est pas un PDF valide");
        }

        return pdfCompressionService.compress(pdfBytes);
    }
}
