package hse.antiplagiat.analysis.repository;

import hse.antiplagiat.analysis.model.AnalysisResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResultEntity, UUID> {
    boolean existsByFileId(UUID fileId);
    Optional<AnalysisResultEntity> findByFileId(UUID id);
}