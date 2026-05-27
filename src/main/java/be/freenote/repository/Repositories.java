package be.freenote.repository;

import be.freenote.exception.ResourceNotFoundException;
import org.springframework.data.repository.CrudRepository;

public final class Repositories {

    private Repositories() {
    }

    public static <T, ID> T findByIdOrThrow(CrudRepository<T, ID> repo, ID id, String entityName) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(entityName, "id", id));
    }
}
