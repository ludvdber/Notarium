package be.freenote.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Validates and prepares uploaded PDF files before storage.
 * Checks MIME type, file size, PDF magic bytes, then compresses.
 */
public interface PdfValidationService {

    /**
     * Validates the uploaded file and returns compressed PDF bytes.
     *
     * @throws IllegalArgumentException  if the file is not a valid PDF
     * @throws be.freenote.exception.PayloadTooLargeException if the file exceeds the size limit
     */
    byte[] validateAndCompress(MultipartFile file);
}
