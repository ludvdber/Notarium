package be.freenote.service;

import be.freenote.dto.response.SectionResponse;

import java.util.List;

public interface SectionService {
    List<SectionResponse> getAll();
    List<SectionResponse> getAllForAdmin();
    SectionResponse getById(Long id);
    SectionResponse create(String name, String icon);
    SectionResponse approve(Long id);
    SectionResponse rename(Long id, String name, String icon);
    void adminDelete(Long id);
}
