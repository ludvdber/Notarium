package be.freenote.service.impl;

import be.freenote.dto.response.DocumentResponse;
import be.freenote.dto.response.PageResponse;
import be.freenote.entity.Document;
import be.freenote.entity.Favorite;
import be.freenote.entity.User;
import be.freenote.mapper.DocumentMapper;
import be.freenote.repository.*;
import be.freenote.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;

    @Override
    @Transactional
    public boolean toggle(Long userId, Long documentId) {
        User user = Repositories.findByIdOrThrow(userRepository, userId, "User");
        Document document = Repositories.findByIdOrThrow(documentRepository, documentId, "Document");

        if (favoriteRepository.existsByUserIdAndDocumentId(userId, documentId)) {
            favoriteRepository.deleteByUserIdAndDocumentId(userId, documentId);
            return false;
        } else {
            Favorite favorite = Favorite.builder()
                    .user(user)
                    .document(document)
                    .build();
            favoriteRepository.save(favorite);
            return true;
        }
    }

    @Override
    public boolean isFavorite(Long userId, Long documentId) {
        return favoriteRepository.existsByUserIdAndDocumentId(userId, documentId);
    }

    @Override
    public PageResponse<DocumentResponse> getFavorites(Long userId, Pageable pageable) {
        Page<Favorite> page = favoriteRepository.findByUserId(userId, pageable);

        List<DocumentResponse> content = page.getContent().stream()
                .map(fav -> documentMapper.toResponse(fav.getDocument()))
                .toList();

        return new PageResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
