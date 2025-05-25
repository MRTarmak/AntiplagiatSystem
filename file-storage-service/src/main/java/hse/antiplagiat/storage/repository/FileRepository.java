package hse.antiplagiat.storage.repository;

import hse.antiplagiat.storage.model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FileRepository extends JpaRepository<FileEntity, UUID> {
    Optional<FileEntity> findByHash(String hash);
    boolean existsByHash(String hash);
}
