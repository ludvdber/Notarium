package be.freenote.service;

import org.springframework.web.multipart.MultipartFile;

public interface PdfValidationService {

    /**
     * Validates the uploaded file and returns its bytes.
     *
     * @throws IllegalArgumentException  if the file is not a valid PDF
     * @throws be.freenote.exception.PayloadTooLargeException if the file exceeds the size limit
     */
    byte[] validate(MultipartFile file);
}
