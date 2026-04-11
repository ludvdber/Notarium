package be.notarium.service;

public interface PdfCompressionService {
    byte[] compress(byte[] pdfBytes);
}
