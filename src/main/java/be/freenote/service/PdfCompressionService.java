package be.freenote.service;

public interface PdfCompressionService {
    byte[] compress(byte[] pdfBytes);
}
