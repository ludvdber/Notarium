package be.notarium.service;

import be.notarium.dto.response.SectionResponse;

import java.util.List;

public interface SectionService {
    List<SectionResponse> getAll();
    SectionResponse getById(Long id);
    SectionResponse create(String name, String icon);
    SectionResponse approve(Long id);
}
