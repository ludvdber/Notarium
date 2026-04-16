package be.freenote.service;

import be.freenote.entity.Document;
import be.freenote.entity.Favorite;
import be.freenote.entity.User;
import be.freenote.enums.Category;
import be.freenote.exception.ResourceNotFoundException;
import be.freenote.mapper.DocumentMapper;
import be.freenote.repository.DocumentRepository;
import be.freenote.repository.FavoriteRepository;
import be.freenote.repository.UserRepository;
import be.freenote.service.impl.FavoriteServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceImplTest {

    @Mock private FavoriteRepository favoriteRepository;
    @Mock private UserRepository userRepository;
    @Mock private DocumentRepository documentRepository;
    @Mock private DocumentMapper documentMapper;

    @InjectMocks private FavoriteServiceImpl favoriteService;

    private User testUser() {
        return User.builder().id(1L).username("user").build();
    }

    private Document testDoc() {
        return Document.builder().id(100L).title("Doc").category(Category.SYNTHESE)
                .fileKey("k").fileSize(100L).build();
    }

    @Test
    void shouldAddFavoriteWhenNotExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser()));
        when(documentRepository.findById(100L)).thenReturn(Optional.of(testDoc()));
        when(favoriteRepository.existsByUserIdAndDocumentId(1L, 100L)).thenReturn(false);

        boolean result = favoriteService.toggle(1L, 100L);

        assertThat(result).isTrue();
        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    void shouldRemoveFavoriteWhenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser()));
        when(documentRepository.findById(100L)).thenReturn(Optional.of(testDoc()));
        when(favoriteRepository.existsByUserIdAndDocumentId(1L, 100L)).thenReturn(true);

        boolean result = favoriteService.toggle(1L, 100L);

        assertThat(result).isFalse();
        verify(favoriteRepository).deleteByUserIdAndDocumentId(1L, 100L);
    }

    @Test
    void shouldThrowWhenTogglingFavoriteForNonExistentUser() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoriteService.toggle(999L, 100L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowWhenTogglingFavoriteForNonExistentDocument() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser()));
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoriteService.toggle(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
