package be.freenote.service;

import be.freenote.dto.response.SectionResponse;

import java.util.List;

public interface SectionService {
    List<SectionResponse> getAll();
    SectionResponse getById(Long id);
    SectionResponse create(String name, String icon);
    SectionResponse approve(Long id);
}
