package be.freenote.service.impl;

import be.freenote.repository.TagRepository;
import be.freenote.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    public List<String> getAllLabels() {
        return tagRepository.findDistinctLabels();
    }
}
